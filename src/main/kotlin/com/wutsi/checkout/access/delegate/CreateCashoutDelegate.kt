package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.CreateCashoutRequest
import com.wutsi.checkout.access.dto.CreateCashoutResponse
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.checkout.access.service.TransactionService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateCashoutDelegate(
    private val service: TransactionService,
    private val logger: KVLogger
) {
    @Transactional(noRollbackFor = [TransactionException::class])
    fun invoke(request: CreateCashoutRequest): CreateCashoutResponse {
        logger.add("business_id", request.businessId)
        logger.add("amount", request.amount)
        logger.add("payment_token", request.paymentMethodToken)
        logger.add("description", request.description)
        logger.add("idempotency_key", request.idempotencyKey)
        logger.add("customer_email", request.customerEmail)
        logger.add("device_id", request.deviceId)

        val tx = service.cashout(request)
        logger.add("transaction_id", tx.id)
        logger.add("transaction_status", tx.status)
        return CreateCashoutResponse(
            transactionId = tx.id ?: "",
            status = tx.status.name
        )
    }
}
