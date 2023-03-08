package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateCashoutRequest
import com.wutsi.checkout.access.dto.CreateChargeRequest
import com.wutsi.checkout.access.dto.CreateDonationRequest
import com.wutsi.checkout.access.dto.SearchTransactionRequest
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.entity.TransactionEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.InsuffisantFundsException
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.payment.Gateway
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
import java.util.Date
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
    private val tracingContext: TracingContext,
) {
    fun donate(request: CreateDonationRequest): TransactionEntity =
        findByIdempotencyKey(request.idempotencyKey)
            .orElseGet {
                doDonate(request)
            }

    fun doDonate(request: CreateDonationRequest): TransactionEntity {
        validate(request)

        val business = businessService.findById(request.businessId)
        val paymentMethod = request.paymentMethodToken?.let {
            paymentMethodService.findByToken(it)
        }

        val gateway = gatewayProvider.get(
            paymentMethod?.type ?: PaymentMethodType.valueOf(request.paymentMethodType!!),
        )
        val paymentMethodNumber = paymentMethod?.number ?: request.paymenMethodNumber!!
        val paymentMethodType = paymentMethod?.type ?: PaymentMethodType.valueOf(request.paymentMethodType!!)
        val tx = dao.save(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                business = business,
                paymentMethod = paymentMethod,
                type = TransactionType.DONATION,
                currency = business.currency,
                description = request.description,
                idempotencyKey = request.idempotencyKey,
                customerAccountId = paymentMethod?.accountId,
                status = Status.UNKNOWN,
                gatewayType = gateway.getType(),
                amount = request.amount,
                fees = 0,
                net = request.amount,

                paymentMethodNumber = paymentMethodNumber,
                paymentMethodCountry = paymentMethod?.country,
                paymentMethodType = paymentMethodType,
                paymentMethodOwnerName = paymentMethod?.ownerName ?: request.paymentMethodOwnerName!!,
                paymentProvider = paymentMethod?.provider
                    ?: paymentProviderService.findById(request.paymentProviderId!!),
                email = request.email,
            ),
        )

        // Donate to business
        createPayment(tx, gateway)
        return tx
    }

    private fun validate(request: CreateDonationRequest) {
        if (request.paymentMethodToken == null) {
            if (request.paymentMethodOwnerName.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_OWNER_NAME_MISSING)
            }
            if (request.paymentMethodType.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_TYPE_MISSING)
            } else {
                try {
                    PaymentMethodType.valueOf(request.paymentMethodType)
                } catch (ex: Exception) {
                    throw badRequest(ErrorURN.PAYMENT_METHOD_TYPE_INVALID)
                }
            }
            if (request.paymenMethodNumber.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_NUMBER_MISSING)
            }
        }
    }

    fun charge(request: CreateChargeRequest): TransactionEntity =
        findByIdempotencyKey(request.idempotencyKey)
            .orElseGet {
                doCharge(request)
            }

    private fun doCharge(request: CreateChargeRequest): TransactionEntity {
        validate(request)

        val business = businessService.findById(request.businessId)
        val paymentMethod = request.paymentMethodToken?.let {
            paymentMethodService.findByToken(it)
        }

        val gateway = gatewayProvider.get(
            paymentMethod?.type ?: PaymentMethodType.valueOf(request.paymentMethodType!!),
        )
        val order = orderService.findById(request.orderId)
        val paymentMethodNumber = paymentMethod?.number ?: request.paymenMethodNumber!!
        val paymentMethodType = paymentMethod?.type ?: PaymentMethodType.valueOf(request.paymentMethodType!!)
        val tx = dao.save(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                business = business,
                paymentMethod = paymentMethod,
                order = order,
                type = TransactionType.CHARGE,
                currency = business.currency,
                description = request.description,
                idempotencyKey = request.idempotencyKey,
                customerAccountId = paymentMethod?.accountId,
                status = Status.UNKNOWN,
                gatewayType = gateway.getType(),
                amount = request.amount,
                fees = 0,
                net = request.amount,

                paymentMethodNumber = paymentMethodNumber,
                paymentMethodCountry = paymentMethod?.country,
                paymentMethodType = paymentMethodType,
                paymentMethodOwnerName = paymentMethod?.ownerName ?: request.paymentMethodOwnerName!!,
                paymentProvider = paymentMethod?.provider
                    ?: paymentProviderService.findById(request.paymentProviderId!!),
                email = request.email,
            ),
        )

        // Charge the customer
        createPayment(tx, gateway)
        return tx
    }

    private fun createPayment(tx: TransactionEntity, gateway: Gateway) {
        try {
            val response = gateway.createPayment(
                request = CreatePaymentRequest(
                    payer = Party(
                        fullName = tx.paymentMethodOwnerName,
                        phoneNumber = tx.paymentMethodNumber,
                        email = tx.email,
                        country = tx.paymentMethodCountry,
                    ),
                    amount = Money(tx.amount.toDouble(), tx.currency),
                    externalId = tx.id!!,
                    description = tx.description ?: "",
                    deviceId = tracingContext.deviceId(),
                    payerMessage = "",
                ),
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
                        "transaction-id" to tx.id!!,
                    ),
                ),
            )
        }
    }

    private fun validate(request: CreateChargeRequest) {
        if (request.paymentMethodToken == null) {
            if (request.paymentMethodOwnerName.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_OWNER_NAME_MISSING)
            }
            if (request.paymentMethodType.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_TYPE_MISSING)
            } else {
                try {
                    PaymentMethodType.valueOf(request.paymentMethodType)
                } catch (ex: Exception) {
                    throw badRequest(ErrorURN.PAYMENT_METHOD_TYPE_INVALID)
                }
            }
            if (request.paymenMethodNumber.isNullOrEmpty()) {
                throw badRequest(ErrorURN.PAYMENT_METHOD_NUMBER_MISSING)
            }
        }
    }

    private fun badRequest(error: ErrorURN) = BadRequestException(
        error = Error(
            code = error.urn,
        ),
    )

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
        val tx = dao.save(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                business = business,
                paymentMethod = paymentMethod,
                type = TransactionType.CASHOUT,
                currency = business.currency,
                description = request.description,
                idempotencyKey = request.idempotencyKey,
                customerAccountId = paymentMethod.accountId,
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
                email = request.email,
            ),
        )

        // Remove the money from the business wallet
        try {
            checkCashoutBalance(business, request)
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
                    payerMessage = "",
                ),
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

    private fun checkCashoutBalance(business: BusinessEntity, request: CreateCashoutRequest) {
        val balance = businessService.computeCashoutBalance(business)
        if (request.amount > balance) {
            throw InsuffisantFundsException()
        }
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
                            type = ParameterType.PARAMETER_TYPE_PATH,
                        ),
                    ),
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
                            "transaction-id" to (tx.id ?: ""),
                        ),
                    ),
                )
            }
            return Optional.of(tx)
        }
        return Optional.empty()
    }

    fun syncStatus(id: String): Status {
        val tx = findById(id)
        if (tx.status == Status.PENDING) {
            when (tx.type) {
                TransactionType.CHARGE, TransactionType.DONATION -> return syncPaymentStatus(tx)
                TransactionType.CASHOUT -> return syncTransferStatus(tx)
                else -> {}
            }
        }
        return tx.status
    }

    private fun syncPaymentStatus(tx: TransactionEntity): Status {
        tx.gatewayTransactionId ?: return tx.status
        try {
            val response = gatewayProvider.get(tx.paymentMethodType).getPayment(tx.gatewayTransactionId!!)
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(
                    tx = tx,
                    response = CreatePaymentResponse(
                        transactionId = tx.gatewayTransactionId!!,
                        financialTransactionId = response.financialTransactionId,
                        status = response.status,
                        fees = response.fees,
                    ),
                )
            }
            return response.status
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
            throw TransactionException(
                error = Error(
                    code = ErrorURN.TRANSACTION_FAILED.urn,
                    downstreamCode = ex.error.code.name,
                    data = mapOf(
                        "transaction-id" to tx.id!!,
                    ),
                ),
            )
        }
    }

    private fun syncTransferStatus(tx: TransactionEntity): Status {
        tx.gatewayTransactionId ?: return tx.status
        try {
            val response = gatewayProvider.get(tx.paymentMethodType).getTransfer(tx.gatewayTransactionId!!)
            if (response.status == Status.SUCCESSFUL) {
                onSuccess(
                    tx = tx,
                    response = CreateTransferResponse(
                        transactionId = tx.gatewayTransactionId!!,
                        financialTransactionId = response.financialTransactionId,
                        status = response.status,
                        fees = response.fees,
                    ),
                )
            }
            return response.status
        } catch (ex: PaymentException) {
            handlePaymentException(tx, ex)
            throw TransactionException(
                error = Error(
                    code = ErrorURN.TRANSACTION_FAILED.urn,
                    downstreamCode = ex.error.code.name,
                    data = mapOf(
                        "transaction-id" to tx.id!!,
                    ),
                ),
            )
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

        if (request.customerAccountId != null) {
            criteria.add("a.customerAccountId=:customer_account_id")
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
        if (request.customerAccountId != null) {
            query.setParameter("customer_account_id", request.customerAccountId)
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
        email = email,
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
        tx.fees = calculator.compute(tx.type, tx.paymentMethodType, tx.business.country, tx.amount)
        tx.net = tx.amount - tx.fees
        tx.updated = Date()
        dao.save(tx)

        // Update the balance
        businessService.updateBalance(tx.business, tx.net)

        // Update the order
        if (tx.order != null) {
            orderService.updateBalance(tx.order)
        }
    }

    private fun onPending(tx: TransactionEntity, response: CreatePaymentResponse) {
        if (tx.status == Status.PENDING) {
            return
        }

        // Update the transaction
        tx.status = Status.PENDING
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        tx.updated = Date()
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
        tx.updated = Date()
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
        tx.updated = Date()
        dao.save(tx)
    }

    private fun handlePaymentException(tx: TransactionEntity, ex: PaymentException) {
        tx.status = Status.FAILED
        tx.errorCode = ex.error.code.name
        tx.supplierErrorCode = ex.error.supplierErrorCode
        tx.supplierErrorMessage = ex.error.message
        tx.gatewayTransactionId = ex.error.transactionId
        tx.updated = Date()
        dao.save(tx)

        if (tx.type == TransactionType.CASHOUT) {
            businessService.updateBalance(tx.business, tx.net) // Rollback transfer
        }
    }

    private fun handleInsufisantFundsException(tx: TransactionEntity) {
        tx.status = Status.FAILED
        tx.errorCode = ErrorCode.NOT_ENOUGH_FUNDS.name
        tx.gatewayFees = 0L
        tx.updated = Date()
        dao.save(tx)
    }

    private fun createTransactionException(tx: TransactionEntity, downstreamError: ErrorCode, cause: Throwable) =
        TransactionException(
            error = Error(
                code = ErrorURN.TRANSACTION_FAILED.urn,
                downstreamCode = downstreamError.name,
                data = mapOf(
                    "transaction-id" to tx.id!!,
                ),
            ),
            cause,
        )
}
