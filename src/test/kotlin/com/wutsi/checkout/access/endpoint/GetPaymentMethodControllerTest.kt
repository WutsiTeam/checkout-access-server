package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dto.GetPaymentMethodResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import com.wutsi.platform.core.error.ErrorResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/GetPaymentMethodController.sql"])
class GetPaymentMethodControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun get() {
        val response = rest.getForEntity(url("token-300"), GetPaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val payment = response.body!!.paymentMethod
        assertEquals(PaymentMethodType.MOBILE_MONEY.name, payment.type)
        assertEquals(PaymentMethodStatus.ACTIVE.name, payment.status)
        assertEquals(300L, payment.accountId)
        assertEquals("Roger Milla", payment.ownerName)
        assertEquals("CM", payment.country)
        assertEquals("+237690000300", payment.number)
        assertNotNull(payment.created)
        assertNotNull(payment.updated)
        assertNull(payment.deactivated)

        assertEquals("MTN", payment.provider.code)
        assertEquals("MTN", payment.provider.name)
        assertEquals(PaymentMethodType.MOBILE_MONEY.name, payment.provider.type)
        assertEquals(
            "https://prod-wutsi.s3.amazonaws.com/static/wutsi-assets/images/payment-providers/mtn.png",
            payment.provider.logoUrl,
        )
    }

    @Test
    fun alreadyAssigned() {
        val ex = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url("Xxx"), GetPaymentMethodResponse::class.java)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PAYMENT_METHOD_NOT_FOUND.urn, response.error.code)
    }

    private fun url(token: String) = "http://localhost:$port/v1/payment-methods/$token"
}
