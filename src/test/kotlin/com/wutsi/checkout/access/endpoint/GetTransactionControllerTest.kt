package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dto.GetTransactionResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.payment.GatewayType
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/GetTransactionController.sql"])
class GetTransactionControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun get() {
        val response = rest.getForEntity(url("tx-200"), GetTransactionResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val tx = response.body!!.transaction
        assertEquals(1L, tx.business.id)
        assertEquals(11L, tx.business.accountId)
        assertEquals("XAF", tx.business.currency)
        assertEquals("CM", tx.business.country)
        assertEquals(120000, tx.business.balance)
        assertEquals("order-200", tx.orderId)
        assertEquals(200L, tx.customerAccountId)
        assertEquals(500, tx.amount)
        assertEquals(5L, tx.fees)
        assertEquals(10L, tx.gatewayFees)
        assertEquals(495L, tx.net)
        assertEquals("XAF", tx.currency)
        assertEquals(GatewayType.MTN.name, tx.gatewayType)
        assertEquals(TransactionType.CASHOUT.name, tx.type)
        assertEquals(Status.SUCCESSFUL.name, tx.status)
        assertEquals("TX-00000-000-1111", tx.gatewayTransactionId)
        assertEquals("FIN-00000-000-1111", tx.financialTransactionId)
        assertEquals("00000", tx.supplierErrorCode)
        assertEquals("Hello world", tx.description)
        assertEquals(ErrorCode.NOT_ENOUGH_FUNDS.name, tx.errorCode)
        assertEquals("roger.milla@gmail.com", tx.email)

        assertEquals("token-200", tx.paymentMethod.token)
        assertEquals(PaymentMethodType.MOBILE_MONEY.name, tx.paymentMethod.type)
        assertEquals(PaymentMethodStatus.ACTIVE.name, tx.paymentMethod.status)
        assertEquals("+237690000200", tx.paymentMethod.number)
        assertEquals(tx.customerAccountId, tx.paymentMethod.accountId)
        assertEquals("MTN", tx.paymentMethod.provider.name)
        assertEquals("MTN", tx.paymentMethod.provider.code)
    }

    @Test
    fun notFound() {
        val ex = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url("03940394039"), GetTransactionResponse::class.java)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.TRANSACTION_NOT_FOUND.urn, response.error.code)
    }

    private fun url(id: String) = "http://localhost:$port/v1/transactions/$id"
}
