package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateCashoutRequest
import com.wutsi.checkout.access.dto.CreateChargeRequest
import com.wutsi.checkout.access.dto.PaymentMethodSummary
import com.wutsi.checkout.access.dto.SearchTransactionRequest
import com.wutsi.checkout.access.dto.Transaction
import com.wutsi.checkout.access.dto.TransactionSummary
import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.entity.TransactionEntity
import com.wutsi.checkout.access.enums.TransactionType
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.InsuffisantFundsException
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.CreateTransferRequest
import com.wutsi.platform.payment.model.CreateTransferResponse
import com.wutsi.platform.payment.model.Party
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID
import javax.persistence.EntityManager
import javax.persistence.Query

@Service
class TransactionService(
    private val dao: TransactionRepository,
    private val gatewayProvider: PaymentGatewayProvider,
    private val businessService: BusinessService,
    private val paymentMethodService: PaymentMethodService,
    private val paymentProviderService: PaymentProviderService,
    private val orderService: OrderService,
    private val calculator: FeesCalculator,
    private val em: EntityManager,
    private val tracingContext: TracingContext
) {
    fun charge(request: CreateChargeRequest): TransactionEntity =
        findByIdempotencyKey(request.idempotencyKey)
            .orElseGet {
                doCharge(request)
            }

    private fun doCharge(request: CreateChargeRequest): TransactionEntity {
        val business = businessService.findById(request.businessId)
        val paymentMethod = paymentMethodService.findByToken(request.paymentMethodToken)
        val gateway = gatewayProvider.get(paymentMethod.type)
        val order = orderService.findById(request.orderId)
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            business = business,
            paymentMethod = paymentMethod,
            order = order,
            type = TransactionType.CHARGE,
            currency = business.currency,
            description = request.description,
            idempotencyKey = request.idempotencyKey,
            customerId = paymentMethod.accountId,
            status = Status.UNKNOWN,
            gatewayType = gateway.getType(),
            amount = request.amount,
            fees = 0,
            net = request.amount,

            paymentMethodNumber = paymentMethod.number,
            paymentMethodCountry = paymentMethod.country,
            paymentMethodType = paymentMethod.type,
            paymentMethodOwnerName = paymentMethod.ownerName,
            paymentProvider = paymentMethod.provider,
            email = request.email
        )

        // Charge the customer
        try {
            val response = gateway.createPayment(
                request = CreatePaymentRequest(
                    payer = Party(
                        fullName = tx.paymentMethodOwnerName,
                        phoneNumber = tx.paymentMethodNumber,
                        email = request.email,
                        country = tx.paymentMethodCountry
                    ),
                    amount = Money(tx.amount.toDouble(), tx.currency),
                    externalId = tx.id!!,
                    description = request.description ?: "",
                    deviceId = tracingContext.deviceId(),
                    payerMessage = ""
                )
            )
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(tx, response)
            } else if (response.status == Status.PENDING) {
                onPending(tx, response)
            }
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
            throw TransactionException(
                error = Error(
                    code = ErrorURN.TRANSACTION_FAILED.urn,
                    downstreamCode = ex.error.code.name,
                    data = mapOf(
                        "transaction-id" to tx.id!!
                    )
                )
            )
        }

        return tx
    }

    fun cashout(request: CreateCashoutRequest): TransactionEntity =
        findByIdempotencyKey(request.idempotencyKey)
            .orElseGet {
                doCashout(request)
            }

    private fun doCashout(request: CreateCashoutRequest): TransactionEntity {
        val business = businessService.findById(request.businessId)
        val paymentMethod = paymentMethodService.findByToken(request.paymentMethodToken)
        val gateway = gatewayProvider.get(paymentMethod.type)
        val fees = calculator.compute(TransactionType.CASHOUT, paymentMethod.type, business.country, request.amount)
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            business = business,
            paymentMethod = paymentMethod,
            type = TransactionType.CASHOUT,
            currency = business.currency,
            description = request.description,
            idempotencyKey = request.idempotencyKey,
            customerId = paymentMethod.accountId,
            status = Status.UNKNOWN,
            gatewayType = gateway.getType(),
            amount = request.amount,
            fees = fees,
            net = request.amount - fees,

            paymentMethodNumber = paymentMethod.number,
            paymentMethodCountry = paymentMethod.country,
            paymentMethodType = paymentMethod.type,
            paymentMethodOwnerName = paymentMethod.ownerName,
            paymentProvider = paymentMethod.provider,
            email = request.email
        )

        // Remove the money from the business wallet
        try {
            businessService.updateBalance(business, -tx.net)
        } catch (ex: InsuffisantFundsException) {
            handleInsufisantFundsException(tx)
            throw createTransactionException(tx, ErrorCode.NOT_ENOUGH_FUNDS, ex)
        }

        // Transfer the money to the account
        try {
            val response = gateway.createTransfer(
                request = CreateTransferRequest(
                    payee = toParty(paymentMethod, request.email),
                    amount = Money(tx.amount.toDouble(), tx.currency),
                    externalId = tx.id!!,
                    description = request.description ?: "",
                    payerMessage = ""
                )
            )
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(tx, response)
            } else if (response.status == Status.PENDING) {
                onPending(tx, response)
            }
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
            throw createTransactionException(tx, ex.error.code, ex)
        }

        return tx
    }

    fun findById(id: String): TransactionEntity =
        dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.TRANSACTION_NOT_FOUND.urn,
                        parameter = Parameter(
                            name = "id",
                            value = id,
                            type = ParameterType.PARAMETER_TYPE_PATH
                        )
                    )
                )
            }

    private fun findByIdempotencyKey(idempotencyKey: String): Optional<TransactionEntity> {
        val opt = dao.findByIdempotencyKey(idempotencyKey)
        if (opt.isPresent) {
            val tx = opt.get()
            if (tx.status == Status.FAILED) {
                throw TransactionException(
                    error = Error(
                        code = ErrorURN.TRANSACTION_FAILED.urn,
                        downstreamCode = tx.errorCode,
                        data = mapOf(
                            "transaction-id" to (tx.id ?: "")
                        )
                    )
                )
            }
            return Optional.of(tx)
        }
        return Optional.empty()
    }

    fun toTransaction(tx: TransactionEntity): Transaction {
        paymentMethodService.toPaymentMethodSummary(tx.paymentMethod)
        return Transaction(
            id = tx.id ?: "",
            financialTransactionId = tx.financialTransactionId,
            amount = tx.amount,
            businessId = tx.business.id ?: -1,
            status = tx.status.name,
            currency = tx.currency,
            created = tx.created.toInstant().atOffset(ZoneOffset.UTC),
            updated = tx.updated.toInstant().atOffset(ZoneOffset.UTC),
            type = tx.type.name,
            description = tx.description,
            errorCode = tx.errorCode,
            fees = tx.fees,
            orderId = tx.order?.id,
            gatewayFees = tx.gatewayFees,
            supplierErrorCode = tx.supplierErrorCode,
            net = tx.net,
            gatewayTransactionId = tx.gatewayTransactionId,
            customerId = tx.customerId,
            gatewayType = tx.gatewayType.name,
            email = tx.email,
            paymentMethod = toPaymentMethodSummary(tx)
        )
    }

    fun toTransactionSummary(tx: TransactionEntity) = TransactionSummary(
        id = tx.id ?: "",
        amount = tx.amount,
        businessId = tx.business.id ?: -1,
        status = tx.status.name,
        currency = tx.currency,
        created = tx.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = tx.updated.toInstant().atOffset(ZoneOffset.UTC),
        type = tx.type.name,
        description = tx.description,
        fees = tx.fees,
        orderId = tx.order?.id,
        gatewayFees = tx.gatewayFees,
        net = tx.net,
        customerId = tx.customerId,
        paymentMethod = toPaymentMethodSummary(tx)
    )

    private fun toPaymentMethodSummary(tx: TransactionEntity) = PaymentMethodSummary(
        token = tx.paymentMethod.token,
        accountId = tx.paymentMethod.accountId,
        number = tx.paymentMethodNumber,
        status = tx.paymentMethod.status.name,

        type = tx.paymentMethodType.name,
        ownerName = tx.paymentMethodOwnerName,
        provider = paymentProviderService.toPaymentProviderSummary(tx.paymentProvider)
    )

    fun syncStatus(id: String) {
        val tx = findById(id)
        if (tx.status == Status.PENDING) {
            when (tx.type) {
                TransactionType.CHARGE -> syncChargeStatus(tx)
                TransactionType.CASHOUT -> syncCashoutStatus(tx)
                else -> {}
            }
        }
    }

    private fun syncChargeStatus(tx: TransactionEntity) {
        tx.gatewayTransactionId ?: return
        try {
            val response = gatewayProvider.get(tx.paymentMethod.type).getPayment(tx.gatewayTransactionId!!)
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(
                    tx = tx,
                    response = CreatePaymentResponse(
                        transactionId = tx.gatewayTransactionId!!,
                        financialTransactionId = response.financialTransactionId,
                        status = response.status,
                        fees = response.fees
                    )
                )
            }
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
        }
    }

    private fun syncCashoutStatus(tx: TransactionEntity) {
        tx.gatewayTransactionId ?: return
        try {
            val response = gatewayProvider.get(tx.paymentMethod.type).getTransfer(tx.gatewayTransactionId!!)
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(
                    tx = tx,
                    response = CreateTransferResponse(
                        transactionId = tx.gatewayTransactionId!!,
                        financialTransactionId = response.financialTransactionId,
                        status = response.status,
                        fees = response.fees
                    )
                )
            }
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
        }
    }

    fun search(request: SearchTransactionRequest): List<TransactionEntity> {
        val sql = sql(request)
        val query = em.createQuery(sql)
        parameters(request, query)
        return query
            .setFirstResult(request.offset)
            .setMaxResults(request.limit)
            .resultList as List<TransactionEntity>
    }

    private fun sql(request: SearchTransactionRequest): String {
        val select = select()
        val where = where(request)
        return if (where.isNullOrEmpty()) {
            select
        } else {
            "$select WHERE $where ORDER BY a.created DESC"
        }
    }

    private fun select(): String =
        "SELECT a FROM TransactionEntity a"

    private fun where(request: SearchTransactionRequest): String {
        val criteria = mutableListOf<String>()

        if (request.customerId != null) {
            criteria.add("a.customerId=:customer_id")
        }
        if (request.businessId != null) {
            criteria.add("a.business.id=:business_id")
        }
        if (request.status.isNotEmpty()) {
            criteria.add("a.status IN :status")
        }
        if (request.type != null) {
            criteria.add("a.type=:type")
        }
        if (request.orderId != null) {
            criteria.add("a.order.id=:order_id")
        }
        return criteria.joinToString(separator = " AND ")
    }

    private fun parameters(request: SearchTransactionRequest, query: Query) {
        if (request.customerId != null) {
            query.setParameter("customer_id", request.customerId)
        }
        if (request.businessId != null) {
            query.setParameter("business_id", request.businessId)
        }
        if (request.status.isNotEmpty()) {
            query.setParameter("status", request.status.map { Status.valueOf(it.uppercase()) })
        }
        if (request.type != null) {
            query.setParameter("type", TransactionType.valueOf(request.type.uppercase()))
        }
        if (request.orderId != null) {
            query.setParameter("order_id", request.orderId)
        }
    }

    private fun toParty(paymentMethod: PaymentMethodEntity, email: String?) = Party(
        fullName = paymentMethod.ownerName,
        country = paymentMethod.country,
        phoneNumber = paymentMethod.number,
        email = email
    )

    private fun onSuccess(tx: TransactionEntity, response: CreatePaymentResponse) {
        if (tx.status == Status.SUCCESSFUL) {
            return
        }

        // Update the transaction
        tx.status = Status.SUCCESSFUL
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        tx.gatewayFees = response.fees.value.toLong()
        tx.fees = calculator.compute(tx.type, tx.paymentMethod.type, tx.business.country, tx.amount)
        tx.net = tx.amount - tx.fees
        dao.save(tx)

        // Update the balance
        businessService.updateBalance(tx.business, tx.net)
    }

    private fun onPending(tx: TransactionEntity, response: CreatePaymentResponse) {
        if (tx.status == Status.PENDING) {
            return
        }

        // Update the transaction
        tx.status = Status.PENDING
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        dao.save(tx)
    }

    private fun onSuccess(tx: TransactionEntity, response: CreateTransferResponse) {
        if (tx.status == Status.SUCCESSFUL) {
            return
        }

        // Update the transaction
        tx.status = Status.SUCCESSFUL
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        tx.gatewayFees = response.fees.value.toLong()
        dao.save(tx)
    }

    private fun onPending(tx: TransactionEntity, response: CreateTransferResponse) {
        if (tx.status == Status.PENDING) {
            return
        }

        // Update the transaction
        tx.status = Status.PENDING
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        dao.save(tx)
    }

    private fun handlePaymentException(tx: TransactionEntity, ex: PaymentException) {
        tx.status = Status.FAILED
        tx.errorCode = ex.error.code.name
        tx.supplierErrorCode = ex.error.supplierErrorCode
        tx.gatewayTransactionId = ex.error.transactionId
        dao.save(tx)

        if (tx.type == TransactionType.CASHOUT) {
            businessService.updateBalance(tx.business, tx.net) // Rollback transfer
        }
    }

    private fun handleInsufisantFundsException(tx: TransactionEntity) {
        tx.status = Status.FAILED
        tx.errorCode = ErrorCode.NOT_ENOUGH_FUNDS.name
        tx.gatewayFees = 0L
        dao.save(tx)
    }

    private fun createTransactionException(tx: TransactionEntity, downstreamError: ErrorCode, cause: Throwable) =
        TransactionException(
            error = Error(
                code = ErrorURN.TRANSACTION_FAILED.urn,
                downstreamCode = downstreamError.name,
                data = mapOf(
                    "transaction-id" to tx.id!!
                )
            ),
            cause
        )
}
