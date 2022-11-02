package com.wutsi.checkout.access.`delegate`

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
    private val logger: KVLogger
) {
    @Transactional(noRollbackFor = [TransactionException::class])
    public fun invoke(request: CreateChargeRequest): CreateChargeResponse {
        logger.add("order_id", request.orderId)
        logger.add("business_id", request.businessId)
        logger.add("amount", request.amount)
        logger.add("payment_token", request.paymentMethodToken)
        logger.add("description", request.description)
        logger.add("idempotency_key", request.idempotencyKey)
        logger.add("customer_email", request.customerEmail)
        logger.add("device_id", request.deviceId)

        val tx = service.charge(request)
        return CreateChargeResponse(
            transactionId = tx.id ?: "",
            status = tx.status.name
        )
    }
}
