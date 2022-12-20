package com.wutsi.checkout.access.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.SyncTransactionStatusResponse
import com.wutsi.checkout.access.service.FeesCalculator
import com.wutsi.platform.payment.GatewayType
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.core.Error
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.GetPaymentResponse
import com.wutsi.platform.payment.model.GetTransferResponse
import com.wutsi.platform.payment.provider.flutterwave.FWGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SyncTransactionStatusControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()
    private val fees = 500L

    @MockBean
    private lateinit var gateway: FWGateway

    @MockBean
    private lateinit var calculator: FeesCalculator

    @Autowired
    private lateinit var dao: TransactionRepository

    @Autowired
    private lateinit var businessDao: BusinessRepository

    @BeforeEach
    fun setUp() {
        doReturn(GatewayType.FLUTTERWAVE).whenever(gateway).getType()
        doReturn(fees).whenever(calculator).compute(any(), any(), any(), any())
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `charge - PENDING to SUCCESSFUL`() {
        // GIVEN
        val paymentResponse = GetPaymentResponse(
            financialTransactionId = "320320",
            status = Status.SUCCESSFUL,
            fees = Money(100.0, "XAF"),
        )
        doReturn(paymentResponse).whenever(gateway).getPayment(any())

        // WHEN
        val response = rest.getForEntity(url("tx-102"), SyncTransactionStatusResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val tx = dao.findById("tx-102").get()
        assertEquals(1500L, tx.amount)
        assertEquals(fees, tx.fees)
        assertEquals(1500L - fees, tx.net)
        assertEquals(paymentResponse.fees.value.toLong(), tx.gatewayFees)
        assertEquals(Status.SUCCESSFUL, tx.status)
        assertEquals(paymentResponse.financialTransactionId, tx.financialTransactionId)
        assertEquals("TX-00000-000-102", tx.gatewayTransactionId)
        assertNull(tx.errorCode)
        assertNull(tx.supplierErrorCode)

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000 + tx.net, business.get().balance)

        assertEquals(tx.status.name, response.body!!.status)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `charge - PENDING to PENDING`() {
        // GIVEN
        val now = Date()
        val paymentResponse = GetPaymentResponse(
            status = Status.PENDING,
        )
        doReturn(paymentResponse).whenever(gateway).getPayment(any())

        // WHEN
        Thread.sleep(2000)
        val response = rest.getForEntity(url("tx-102"), SyncTransactionStatusResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val tx = dao.findById("tx-102").get()
        assertTrue(tx.updated.before(now))

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000, business.get().balance)

        assertEquals(tx.status.name, response.body!!.status)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `charge - PENDING to FAILED`() {
        // GIVEN
        val ex = PaymentException(
            error = Error(
                code = ErrorCode.DECLINED,
                transactionId = "13203209",
                supplierErrorCode = "1111",
                errorId = "111112",
            ),
        )
        doThrow(ex).whenever(gateway).getPayment(any())

        // WHEN
        val e = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url("tx-102"), Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, e.statusCode)

        val tx = dao.findById("tx-102").get()
        assertEquals(1500, tx.amount)
        assertEquals(0, tx.fees)
        assertEquals(1500, tx.net)
        assertEquals(0, tx.gatewayFees)
        assertEquals(Status.FAILED, tx.status)
        assertEquals(ex.error.code.name, tx.errorCode)
        assertEquals(ex.error.supplierErrorCode, tx.supplierErrorCode)
        assertEquals(ex.error.transactionId, tx.gatewayTransactionId)
        assertNull(tx.financialTransactionId)

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000, business.get().balance)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `charge - SUCCESSFUL to SUCCESSFUL`() {
        // GIVEN
        val now = Date()
        val paymentResponse = GetPaymentResponse(
            financialTransactionId = "320320",
            status = Status.SUCCESSFUL,
            fees = Money(100.0, "XAF"),
        )
        doReturn(paymentResponse).whenever(gateway).getPayment(any())

        // WHEN
        Thread.sleep(2000)
        val response = rest.getForEntity(url("tx-101"), SyncTransactionStatusResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val tx = dao.findById("tx-101").get()
        assertTrue(tx.updated.before(now))

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000, business.get().balance)

        assertEquals(tx.status.name, response.body!!.status)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `cashout - PENDING to SUCCESSFUL`() {
        // GIVEN
        val paymentResponse = GetTransferResponse(
            financialTransactionId = "320320",
            status = Status.SUCCESSFUL,
            fees = Money(100.0, "XAF"),
        )
        doReturn(paymentResponse).whenever(gateway).getTransfer(any())

        // WHEN
        val response = rest.getForEntity(url("tx-202"), SyncTransactionStatusResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val tx = dao.findById("tx-202").get()
        assertEquals(1500, tx.amount)
        assertEquals(5, tx.fees)
        assertEquals(1500 - 5, tx.net)
        assertEquals(paymentResponse.fees.value.toLong(), tx.gatewayFees)
        assertEquals(Status.SUCCESSFUL, tx.status)
        assertEquals(paymentResponse.financialTransactionId, tx.financialTransactionId)
        assertEquals("TX-00000-000-202", tx.gatewayTransactionId)
        assertNull(tx.errorCode)
        assertNull(tx.supplierErrorCode)

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000, business.get().balance)

        assertEquals(tx.status.name, response.body!!.status)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/SyncTransactionStatusController.sql"])
    fun `cashout - PENDING to FAILED`() {
        // GIVEN
        val ex = PaymentException(
            error = Error(
                code = ErrorCode.DECLINED,
                transactionId = "13203209",
                supplierErrorCode = "1111",
                errorId = "111112",
            ),
        )
        doThrow(ex).whenever(gateway).getTransfer(any())

        // WHEN
        val e = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url("tx-202"), Any::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, e.statusCode)

        val tx = dao.findById("tx-202").get()
        assertEquals(1500, tx.amount)
        assertEquals(5, tx.fees)
        assertEquals(1495, tx.net)
        assertEquals(0, tx.gatewayFees)
        assertEquals(Status.FAILED, tx.status)
        assertEquals(ex.error.code.name, tx.errorCode)
        assertEquals(ex.error.supplierErrorCode, tx.supplierErrorCode)
        assertEquals(ex.error.transactionId, tx.gatewayTransactionId)
        assertNull(tx.financialTransactionId)

        val business = businessDao.findById(tx.business.id)
        assertEquals(120000 + tx.net, business.get().balance)
    }

    private fun url(id: String) = "http://localhost:$port/v1/transactions/$id/status/sync"
}
