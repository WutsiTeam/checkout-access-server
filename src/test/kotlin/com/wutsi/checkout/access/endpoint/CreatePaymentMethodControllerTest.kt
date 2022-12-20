package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dao.PaymentMethodRepository
import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.CreatePaymentMethodResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/CreatePaymentMethodController.sql"])
class CreatePaymentMethodControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: PaymentMethodRepository

    @Test
    fun create() {
        val request = CreatePaymentMethodRequest(
            type = PaymentMethodType.MOBILE_MONEY.name,
            providerId = 1001,
            number = "+237670000111",
            country = "CM",
            ownerName = "RAY SPONSIBLE",
            accountId = 111,
        )
        val response = rest.postForEntity(url(), request, CreatePaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val token = response.body!!.paymentMethodToken
        val payment = dao.findByToken(token)
        assertTrue(payment.isPresent)
        assertEquals(PaymentMethodType.MOBILE_MONEY, payment.get().type)
        assertEquals(PaymentMethodStatus.ACTIVE, payment.get().status)
        assertEquals(request.accountId, payment.get().accountId)
        assertEquals(request.ownerName, payment.get().ownerName)
        assertEquals(request.country, payment.get().country)
        assertEquals(request.number, payment.get().number)
        assertEquals(request.providerId, payment.get().provider.id)
        assertNotNull(payment.get().created)
        assertNotNull(payment.get().updated)
        assertNull(payment.get().deactivated)
    }

    @Test
    fun idempotency() {
        val request = CreatePaymentMethodRequest(
            type = PaymentMethodType.MOBILE_MONEY.name,
            providerId = 1001,
            number = "+237690000300",
            country = "CM",
            ownerName = "RAY SPONSIBLE",
            accountId = 300,
        )
        val response = rest.postForEntity(url(), request, CreatePaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val token = response.body!!.paymentMethodToken
        assertEquals("token-300", token)
    }

    @Test
    fun recycle() {
        val request = CreatePaymentMethodRequest(
            providerId = 1001,
            type = PaymentMethodType.MOBILE_MONEY.name,
            number = "+237690000200",
            country = "CM",
            ownerName = "RAY SPONSIBLE",
            accountId = 200,
        )
        val response = rest.postForEntity(url(), request, CreatePaymentMethodResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val token = response.body!!.paymentMethodToken
        val payment = dao.findByToken(token)
        assertTrue(payment.isPresent)
        assertEquals(PaymentMethodType.MOBILE_MONEY, payment.get().type)
        assertEquals(PaymentMethodStatus.ACTIVE, payment.get().status)
        assertEquals(request.accountId, payment.get().accountId)
        assertEquals(request.ownerName, payment.get().ownerName)
        assertEquals(request.country, payment.get().country)
        assertEquals(request.number, payment.get().number)
        assertEquals(request.providerId, payment.get().provider.id)
        assertNotNull(payment.get().created)
        assertNotNull(payment.get().updated)
        assertNull(payment.get().deactivated)
    }

    @Test
    fun alreadyAssigned() {
        val request = CreatePaymentMethodRequest(
            type = PaymentMethodType.MOBILE_MONEY.name,
            number = "+237690000300",
            providerId = 1000,
            country = "CM",
            ownerName = "RAY SPONSIBLE",
            accountId = 3333,
        )
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, CreatePaymentMethodResponse::class.java)
        }

        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.PAYMENT_METHOD_ALREADY_ASSIGNED.urn, response.error.code)
    }

    private fun url() = "http://localhost:$port/v1/payment-methods"
}
