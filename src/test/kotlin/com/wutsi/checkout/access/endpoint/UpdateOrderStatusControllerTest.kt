package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.enums.OrderStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
            status = OrderStatus.COMPLETED.name,
        )
        val response = rest.postForEntity(url("100"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("100").get()
        assertEquals(OrderStatus.COMPLETED, order.status)
        assertNotNull(order.closed)
    }

    @Test
    fun cancel() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.CANCELLED.name,
            reason = "Yeauuurk",
        )
        val response = rest.postForEntity(url("101"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("101").get()
        assertEquals(OrderStatus.CANCELLED, order.status)
        assertNotNull(order.cancelled)
        assertEquals(request.reason, order.cancellationReason)
    }

    @Test
    fun expire() {
        val request = UpdateOrderStatusRequest(
            status = OrderStatus.EXPIRED.name,
        )
        val response = rest.postForEntity(url("102"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("102").get()
        assertEquals(OrderStatus.EXPIRED, order.status)
        assertNotNull(order.expired)
    }

    @Test
    fun sameStatus() {
        val now = System.currentTimeMillis()
        Thread.sleep(1000)

        val request = UpdateOrderStatusRequest(
            status = OrderStatus.OPENED.name,
            reason = "Yeauuurk",
        )
        val response = rest.postForEntity(url("102"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("102").get()
        assertEquals(OrderStatus.OPENED, order.status)
        assertTrue(order.updated.before(Date(now)))
    }

    private fun url(id: String) = "http://localhost:$port/v1/orders/$id/status"
}
