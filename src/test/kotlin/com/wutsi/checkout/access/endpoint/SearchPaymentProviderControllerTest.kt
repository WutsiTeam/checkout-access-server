package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dto.SearchPaymentProviderRequest
import com.wutsi.checkout.access.dto.SearchPaymentProviderResponse
import com.wutsi.enums.PaymentMethodType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchPaymentProviderControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun byCountryType() {
        val request = SearchPaymentProviderRequest(
            country = "CM",
            type = PaymentMethodType.MOBILE_MONEY.name,
        )
        val response = rest.postForEntity(url(), request, SearchPaymentProviderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val providers = response.body!!.paymentProviders
        assertEquals(2, providers.size)

        assertEquals("MTN", providers[0].code)
        assertEquals("Orange", providers[1].code)
    }

    @Test
    fun byCountryTypeAndNumber() {
        val request = SearchPaymentProviderRequest(
            country = "CM",
            type = PaymentMethodType.MOBILE_MONEY.name,
            number = "+237690000010",
        )
        val response = rest.postForEntity(url(), request, SearchPaymentProviderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val providers = response.body!!.paymentProviders
        assertEquals(1, providers.size)

        assertEquals("Orange", providers[0].code)
    }

    @Test
    fun byTypeAndNumber() {
        val request = SearchPaymentProviderRequest(
            type = PaymentMethodType.MOBILE_MONEY.name,
            number = "+237690000010",
            country = "",
        )
        val response = rest.postForEntity(url(), request, SearchPaymentProviderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val providers = response.body!!.paymentProviders
        assertEquals(1, providers.size)

        assertEquals("Orange", providers[0].code)
    }

    @Test
    fun byCountryTypeAndNumberNotFound() {
        val request = SearchPaymentProviderRequest(
            country = "CM",
            type = PaymentMethodType.MOBILE_MONEY.name,
            number = "+000690000010",
        )
        val response = rest.postForEntity(url(), request, SearchPaymentProviderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val providers = response.body!!.paymentProviders
        assertEquals(0, providers.size)
    }

    private fun url() = "http://localhost:$port/v1/payment-providers/search"
}
