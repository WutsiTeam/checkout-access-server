package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dto.SearchPaymentMethodRequest
import com.wutsi.checkout.access.dto.SearchPaymentMethodResponse
import com.wutsi.enums.PaymentMethodStatus
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/SearchPaymentMethodController.sql"])
class SearchPaymentMethodControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun byAccount() {
        val request = SearchPaymentMethodRequest(
            accountId = 300L,
        )
        val response = rest.postForEntity(url(), request, SearchPaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val paymentMethods = response.body!!.paymentMethods
        assertEquals(3, paymentMethods.size)
        assertEquals(listOf("token-300", "token-301", "token-399"), paymentMethods.map { it.token })
    }

    @Test
    fun byStatus() {
        val request = SearchPaymentMethodRequest(
            accountId = 300L,
            status = PaymentMethodStatus.ACTIVE.name,
        )
        val response = rest.postForEntity(url(), request, SearchPaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val paymentMethods = response.body!!.paymentMethods
        assertEquals(2, paymentMethods.size)
        assertEquals(listOf("token-300", "token-301"), paymentMethods.map { it.token })
    }

    private fun url() = "http://localhost:$port/v1/payment-methods/search"
}
