package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.checkout.access.enums.OrderStatus
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.platform.core.error.ErrorResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/UpdateOrderStatusController.sql"])
class UpdateOrderStatusControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: OrderRepository

    @Test
    fun close() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.CLOSED.name
        )
        val response = rest.postForEntity(url("100"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("100").get()
        assertEquals(OrderStatus.CLOSED, order.status)
        assertNotNull(order.closed)
    }

    @Test
    fun cancel() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.CANCELLED.name,
            reason = "Yeauuurk"
        )
        val response = rest.postForEntity(url("101"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("101").get()
        assertEquals(OrderStatus.CANCELLED, order.status)
        assertNotNull(order.cancelled)
        assertEquals(request.reason, order.cancellationReason)
    }

    @Test
    fun sameStatus() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.OPENED.name,
            reason = "Yeauuurk"
        )
        val response = rest.postForEntity(url("102"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("102").get()
        assertEquals(OrderStatus.OPENED, order.status)
    }

    @Test
    fun alreadyClosed() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.OPENED.name
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url("200"), request, Any::class.java)
        }

        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.ORDER_CLOSED.urn, response.error.code)
    }

    @Test
    fun alreadyCancelled() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.OPENED.name
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url("300"), request, Any::class.java)
        }

        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.ORDER_CLOSED.urn, response.error.code)
    }

    private fun url(id: String) = "http://localhost:$port/v1/orders/$id/status"
}
