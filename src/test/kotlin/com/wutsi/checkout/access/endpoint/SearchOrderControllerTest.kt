package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dto.SearchOrderRequest
import com.wutsi.checkout.access.dto.SearchOrderResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/SearchOrderController.sql"])
public class SearchOrderControllerTest {
    @LocalServerPort
    public val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun searchByCustomer() {
        val request = SearchOrderRequest(
            customerAccountId = 11L,
        )
        val response = rest.postForEntity(url(), request, SearchOrderResponse::class.java)

        assertEquals(200, response.statusCodeValue)

        val orders = response.body!!.orders
        assertEquals(2, orders.size)
        assertTrue(orders.map { it.id }.containsAll(listOf("100", "200")))
    }

    @Test
    fun byBusiness() {
        val request = SearchOrderRequest(
            businessId = 2,
        )
        val response = rest.postForEntity(url(), request, SearchOrderResponse::class.java)

        assertEquals(200, response.statusCodeValue)

        val orders = response.body!!.orders
        assertEquals(1, orders.size)
        assertTrue(orders.map { it.id }.containsAll(listOf("300")))
    }

    @Test
    fun byDates() {
        val request = SearchOrderRequest(
            createdFrom = OffsetDateTime.now().minusDays(90),
            createdTo = OffsetDateTime.now().plusDays(1),
        )
        val response = rest.postForEntity(url(), request, SearchOrderResponse::class.java)

        assertEquals(200, response.statusCodeValue)

        val orders = response.body!!.orders
        assertEquals(1, orders.size)
        assertTrue(orders.map { it.id }.containsAll(listOf("300")))
    }

    @Test
    fun expired() {
        val request = SearchOrderRequest(
            expiresTo = OffsetDateTime.now(),
        )
        val response = rest.postForEntity(url(), request, SearchOrderResponse::class.java)

        assertEquals(200, response.statusCodeValue)

        val orders = response.body!!.orders
        assertEquals(1, orders.size)
        assertTrue(orders.map { it.id }.containsAll(listOf("100")))
    }

    @Test
    fun byProduct() {
        val request = SearchOrderRequest(
            productId = 10L,
        )
        val response = rest.postForEntity(url(), request, SearchOrderResponse::class.java)

        assertEquals(200, response.statusCodeValue)

        val orders = response.body!!.orders
        assertEquals(2, orders.size)
        assertTrue(orders.map { it.id }.containsAll(listOf("100", "200")))
    }

    private fun url() = "http://localhost:$port/v1/orders/search"
}
