package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateChargeRequest
import com.wutsi.checkout.access.dto.CreateChargeResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.service.FeesCalculator
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.payment.GatewayType
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.core.Error
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentResponse
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
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateChargeControllerTest {
    @LocalServerPort
    val port: Int = 0

    @Autowired
    private lateinit var dao: TransactionRepository

    @Autowired
    private lateinit var businessDao: BusinessRepository

    @Autowired
    private lateinit var orderDao: OrderRepository

    @MockBean
    private lateinit var gateway: FWGateway

    @MockBean
    private lateinit var calculator: FeesCalculator

    @MockBean
    private lateinit var tracingContext: TracingContext

    private val fees = 5000L
    private val deviceId = "049049-40394039"
    private val rest = RestTemplate()

    @BeforeEach
    fun setUp() {
        doReturn(GatewayType.FLUTTERWAVE).whenever(gateway).getType()
        doReturn(fees).whenever(calculator).compute(any(), any(), any(), any())

        doReturn(deviceId).whenever(tracingContext).deviceId()
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/CreateChargeController.sql"])
    fun success() {
        // GIVEN
        val paymentResponse = CreatePaymentResponse(
            transactionId = UUID.randomUUID().toString(),
            financialTransactionId = null,
            status = Status.SUCCESSFUL,
            fees = Money(100.0, "XAF"),
        )
        doReturn(paymentResponse).whenever(gateway).createPayment(any())

        // WHEN
        val request = createRequest(amount = 50000)
        val response = rest.postForEntity(url(), request, CreateChargeResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body?.transactionId
        assertEquals(paymentResponse.status.name, response.body?.status)
        assertNotNull(id)

        val tx = dao.findById(id).get()
        assertEquals(1L, tx.business.id)
        assertEquals(request.orderId, tx.order?.id)
        assertEquals(100L, tx.customerAccountId)
        assertEquals(1001L, tx.paymentMethod?.id)
        assertEquals(request.amount, tx.amount)
        assertEquals(fees, tx.fees)
        assertEquals(paymentResponse.fees.value.toLong(), tx.gatewayFees)
        assertEquals(request.amount - fees, tx.net)
        assertEquals("XAF", tx.currency)
        assertEquals(gateway.getType(), tx.gatewayType)
        assertEquals(TransactionType.CHARGE, tx.type)
        assertEquals(Status.SUCCESSFUL, tx.status)
        assertEquals(paymentResponse.transactionId, tx.gatewayTransactionId)
        assertEquals(paymentResponse.financialTransactionId, tx.financialTransactionId)
        assertNull(tx.supplierErrorCode)
        assertEquals(request.description, tx.description)
        assertNull(tx.errorCode)
        assertEquals(request.idempotencyKey, tx.idempotencyKey)
        assertEquals("+237690000100", tx.paymentMethodNumber)
        assertEquals("Roger Milla", tx.paymentMethodOwnerName)
        assertEquals(PaymentMethodType.MOBILE_MONEY, tx.paymentMethodType)
        assertEquals("CM", tx.paymentMethodCountry)
        assertEquals(1000, tx.paymentProvider.id)
        assertEquals(request.email, tx.email)

        val business = businessDao.findById(tx.business.id!!).get()
        assertEquals(120000 + tx.net, business.balance)

        val order = orderDao.findById(tx.order!!.id).get()
        assertEquals(tx.amount, order.totalPaid)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/CreateChargeController.sql"])
    fun pending() {
        val paymentResponse = CreatePaymentResponse(
            transactionId = UUID.randomUUID().toString(),
            financialTransactionId = null,
            status = Status.PENDING,
        )
        doReturn(paymentResponse).whenever(gateway).createPayment(any())

        // WHEN
        val request = createRequest(amount = 50000)
        val response = rest.postForEntity(url(), request, CreateChargeResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body?.transactionId
        assertEquals(paymentResponse.status.name, response.body?.status)
        assertNotNull(id)

        val tx = dao.findById(id).get()
        assertEquals(1L, tx.business.id)
        assertEquals(request.orderId, tx.order?.id)
        assertEquals(100L, tx.customerAccountId)
        assertEquals(1001L, tx.paymentMethod?.id)
        assertEquals(request.amount, tx.amount)
        assertEquals(0L, tx.fees)
        assertEquals(0L, tx.gatewayFees)
        assertEquals(request.amount, tx.net)
        assertEquals("XAF", tx.currency)
        assertEquals(gateway.getType(), tx.gatewayType)
        assertEquals(TransactionType.CHARGE, tx.type)
        assertEquals(Status.PENDING, tx.status)
        assertEquals(paymentResponse.transactionId, tx.gatewayTransactionId)
        assertEquals(paymentResponse.financialTransactionId, tx.financialTransactionId)
        assertNull(tx.supplierErrorCode)
        assertEquals(request.description, tx.description)
        assertNull(tx.errorCode)
        assertEquals(request.idempotencyKey, tx.idempotencyKey)
        assertEquals("+237690000100", tx.paymentMethodNumber)
        assertEquals("Roger Milla", tx.paymentMethodOwnerName)
        assertEquals(PaymentMethodType.MOBILE_MONEY, tx.paymentMethodType)
        assertEquals("CM", tx.paymentMethodCountry)
        assertEquals(1000, tx.paymentProvider.id)
        assertEquals(request.email, tx.email)

        val business = businessDao.findById(tx.business.id!!).get()
        assertEquals(120000, business.balance)

        val order = orderDao.findById(tx.order!!.id).get()
        assertEquals(0, order.totalPaid)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/CreateChargeController.sql"])
    fun failure() {
        // GIVEN
        val e = PaymentException(
            error = Error(
                code = ErrorCode.NOT_ENOUGH_FUNDS,
                transactionId = UUID.randomUUID().toString(),
                supplierErrorCode = "failed",
            ),
        )
        doThrow(e).whenever(gateway).createPayment(any())

        // WHEN
        val request = createRequest(amount = 1000)
        val ex = assertThrows<HttpClientErrorException> {
            rest.postForEntity(url(), request, CreateChargeResponse::class.java)
        }

        // THEN
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.TRANSACTION_FAILED.urn, response.error.code)
        assertEquals(ErrorCode.NOT_ENOUGH_FUNDS.name, response.error.downstreamCode)

        val id = response.error.data?.get("transaction-id")?.toString()
        assertNotNull(id)

        val tx = dao.findById(id).get()
        assertEquals(1L, tx.business.id)
        assertEquals(request.orderId, tx.order?.id)
        assertEquals(100L, tx.customerAccountId)
        assertEquals(1001L, tx.paymentMethod?.id)
        assertEquals(request.amount, tx.amount)
        assertEquals(0L, tx.fees)
        assertEquals(0L, tx.gatewayFees)
        assertEquals(request.amount, tx.net)
        assertEquals("XAF", tx.currency)
        assertEquals(gateway.getType(), tx.gatewayType)
        assertEquals(TransactionType.CHARGE, tx.type)
        assertEquals(Status.FAILED, tx.status)
        assertEquals(e.error.transactionId, tx.gatewayTransactionId)
        assertNull(tx.financialTransactionId)
        assertEquals(e.error.supplierErrorCode, tx.supplierErrorCode)
        assertEquals(request.description, tx.description)
        assertEquals(e.error.code.name, tx.errorCode)
        assertEquals(request.idempotencyKey, tx.idempotencyKey)
        assertEquals("+237690000100", tx.paymentMethodNumber)
        assertEquals("Roger Milla", tx.paymentMethodOwnerName)
        assertEquals(PaymentMethodType.MOBILE_MONEY, tx.paymentMethodType)
        assertEquals("CM", tx.paymentMethodCountry)
        assertEquals(1000, tx.paymentProvider.id)
        assertEquals(request.email, tx.email)

        val business = businessDao.findById(tx.business.id!!).get()
        assertEquals(120000, business.balance)

        val order = orderDao.findById(tx.order!!.id).get()
        assertEquals(0, order.totalPaid)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/CreateChargeController.sql"])
    fun idempotency() {
        // WHEN
        val request = createRequest(amount = 500, idempotencyKey = "idempotent-200")
        val response = rest.postForEntity(url(), request, CreateChargeResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body?.transactionId
        assertNotNull("tx-200", id)

        val business = businessDao.findById(1).get()
        assertEquals(120000, business.balance)
    }

    @Test
    @Sql(value = ["/db/clean.sql", "/db/CreateChargeController.sql"])
    fun `anonymous payment`() {
        // GIVEN
        val paymentResponse = CreatePaymentResponse(
            transactionId = UUID.randomUUID().toString(),
            financialTransactionId = null,
            status = Status.SUCCESSFUL,
            fees = Money(100.0, "XAF"),
        )
        doReturn(paymentResponse).whenever(gateway).createPayment(any())

        // WHEN
        val request = createRequest(
            amount = 50000,
            paymentMethodToken = null,
            paymentMethodType = PaymentMethodType.MOBILE_MONEY.name,
            paymenMethodNumber = "+237671111100",
            paymentMethodOwnerName = "Ray Sponsible",
            paymentProviderId = 1000L,
        )
        val response = rest.postForEntity(url(), request, CreateChargeResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body?.transactionId
        assertEquals(paymentResponse.status.name, response.body?.status)
        assertNotNull(id)

        val tx = dao.findById(id).get()
        assertEquals(1L, tx.business.id)
        assertEquals(request.orderId, tx.order?.id)
        assertNull(tx.customerAccountId)
        assertNull(tx.paymentMethod?.id)
        assertEquals(request.amount, tx.amount)
        assertEquals(fees, tx.fees)
        assertEquals(paymentResponse.fees.value.toLong(), tx.gatewayFees)
        assertEquals(request.amount - fees, tx.net)
        assertEquals("XAF", tx.currency)
        assertEquals(gateway.getType(), tx.gatewayType)
        assertEquals(TransactionType.CHARGE, tx.type)
        assertEquals(Status.SUCCESSFUL, tx.status)
        assertEquals(paymentResponse.transactionId, tx.gatewayTransactionId)
        assertEquals(paymentResponse.financialTransactionId, tx.financialTransactionId)
        assertNull(tx.supplierErrorCode)
        assertEquals(request.description, tx.description)
        assertNull(tx.errorCode)
        assertEquals(request.idempotencyKey, tx.idempotencyKey)
        assertEquals(request.paymenMethodNumber, tx.paymentMethodNumber)
        assertEquals(request.paymentMethodOwnerName, tx.paymentMethodOwnerName)
        assertEquals(request.paymentMethodType, tx.paymentMethodType.name)
        assertNull(tx.paymentMethodCountry)
        assertEquals(request.paymentProviderId, tx.paymentProvider.id)
        assertEquals(request.email, tx.email)

        val business = businessDao.findById(tx.business.id!!).get()
        assertEquals(120000 + tx.net, business.balance)
    }

    private fun createRequest(
        amount: Long = 50000,
        businessId: Long = 1L,
        idempotencyKey: String = UUID.randomUUID().toString(),
        paymentMethodToken: String? = "token-100",
        orderId: String = "order-100",
        paymentMethodOwnerName: String? = null,
        paymentMethodType: String? = null,
        paymentProviderId: Long? = null,
        paymenMethodNumber: String? = null,
    ) = CreateChargeRequest(
        paymentMethodToken = paymentMethodToken,
        businessId = businessId,
        amount = amount,
        description = "Hello world",
        idempotencyKey = idempotencyKey,
        orderId = orderId,
        email = "ray.sponsible@gmail.com",
        paymentMethodOwnerName = paymentMethodOwnerName,
        paymentMethodType = paymentMethodType,
        paymentProviderId = paymentProviderId,
        paymenMethodNumber = paymenMethodNumber,
    )

    private fun url() = "http://localhost:$port/v1/transactions/charge"
}
