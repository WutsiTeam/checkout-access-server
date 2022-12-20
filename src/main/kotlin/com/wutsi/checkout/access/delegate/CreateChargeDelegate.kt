package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.CreateChargeRequest
import com.wutsi.checkout.access.dto.CreateChargeResponse
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.checkout.access.service.TransactionService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
public class CreateChargeDelegate(
    private val service: TransactionService,
    private val logger: KVLogger,
) {
    @Transactional(noRollbackFor = [TransactionException::class])
    public fun invoke(request: CreateChargeRequest): CreateChargeResponse {
        logger.add("request_order_id", request.orderId)
        logger.add("request_business_id", request.businessId)
        logger.add("request_amount", request.amount)
        logger.add("request_payment_token", request.paymentMethodToken)
        logger.add("request_description", request.description)
        logger.add("request_idempotency_key", request.idempotencyKey)
        logger.add("request_customer_email", request.email)

        val tx = service.charge(request)
        logger.add("response_transaction_id", tx.id)
        logger.add("response_transaction_status", tx.status)

        return CreateChargeResponse(
            transactionId = tx.id ?: "",
            status = tx.status.name,
        )
    }
}
