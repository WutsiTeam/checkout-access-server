package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateChargeRequest
import com.wutsi.checkout.access.entity.TransactionEntity
import com.wutsi.checkout.access.enums.TransactionType
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.payment.PaymentException
import com.wutsi.platform.payment.core.Money
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.model.CreatePaymentRequest
import com.wutsi.platform.payment.model.CreatePaymentResponse
import com.wutsi.platform.payment.model.Party
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
class TransactionService(
    private val dao: TransactionRepository,
    private val gatewayProvider: PaymentGatewayProvider,
    private val businessService: BusinessService,
    private val paymentMethodService: PaymentMethodService,
    private val orderService: OrderService,
    private val calculator: FeesCalculator
) {
    fun charge(request: CreateChargeRequest): TransactionEntity {
        // Idempotency
        val txOpt = findIdempotentTransaction(request.idempotencyKey)
        if (txOpt.isPresent) {
            return txOpt.get()
        }

        // Create Transaction
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
            net = request.amount
        )

        // Charge the customer
        try {
            val response = gateway.createPayment(
                request = CreatePaymentRequest(
                    payer = Party(
                        fullName = paymentMethod.ownerName,
                        country = paymentMethod.country,
                        phoneNumber = paymentMethod.number,
                        email = request.customerEmail
                    ),
                    amount = Money(tx.amount.toDouble(), tx.currency),
                    externalId = tx.id!!,
                    description = request.description ?: "",
                    deviceId = request.deviceId,
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

    fun onSuccess(tx: TransactionEntity, response: CreatePaymentResponse) {
        if (tx.status == Status.SUCCESSFUL) {
            return
        }

        // Update the transaction
        tx.status = Status.SUCCESSFUL
        tx.gatewayTransactionId = response.transactionId.ifEmpty { null }
        tx.financialTransactionId = response.financialTransactionId
        tx.gatewayFees = response.fees.value.toLong()
        tx.fees = calculator.computeFees(tx)
        tx.net = tx.amount - tx.fees
        dao.save(tx)

        // Update the balance
        businessService.updateBalance(tx.business, -tx.net)
    }

    fun onPending(tx: TransactionEntity, response: CreatePaymentResponse) {
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
        tx.gatewayFees = 0L
        dao.save(tx)
    }

    private fun findIdempotentTransaction(idempotencyKey: String): Optional<TransactionEntity> {
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
}
