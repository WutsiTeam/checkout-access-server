package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.OrderRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/UpdateOrderBalanceController.sql"])
class UpdateOrderBalanceControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: OrderRepository

    @Test
    fun update() {
        val response = rest.postForEntity(url("order-100"), null, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = dao.findById("order-100").get()
        assertEquals(45000, order.totalPaid)
        assertEquals(50000, order.totalPrice)
    }

    private fun url(id: String) = "http://localhost:$port/v1/orders/$id/balance"
}
