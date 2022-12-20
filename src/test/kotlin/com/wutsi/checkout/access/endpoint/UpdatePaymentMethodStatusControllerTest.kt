package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.PaymentMethodRepository
import com.wutsi.checkout.access.dto.UpdatePaymentMethodStatusRequest
import com.wutsi.enums.PaymentMethodStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/UpdatePaymentMethodStatusController.sql"])
class UpdatePaymentMethodStatusControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: PaymentMethodRepository

    @Test
    fun deactivate() {
        val request = UpdatePaymentMethodStatusRequest(
            status = PaymentMethodStatus.INACTIVE.name,
        )
        val response = rest.postForEntity(url("token-300"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val payment = dao.findByToken("token-300").get()
        assertEquals(PaymentMethodStatus.INACTIVE, payment.status)
        assertNotNull(payment.deactivated)
    }

    @Test
    fun activate() {
        val request = UpdatePaymentMethodStatusRequest(
            status = PaymentMethodStatus.INACTIVE.name,
        )
        val response = rest.postForEntity(url("token-399"), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val payment = dao.findByToken("token-300").get()
        assertEquals(PaymentMethodStatus.ACTIVE, payment.status)
        assertNull(payment.deactivated)
    }

    private fun url(token: String) = "http://localhost:$port/v1/payment-methods/$token/status"
}
