package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.CreateDonationRequest
import com.wutsi.checkout.access.dto.CreateDonationResponse
import com.wutsi.checkout.access.service.TransactionService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
public class CreateDonationDelegate(
    private val logger: KVLogger,
    private val service: TransactionService,
) {
    public fun invoke(request: CreateDonationRequest): CreateDonationResponse {
        logger.add("request_business_id", request.businessId)
        logger.add("request_amount", request.amount)
        logger.add("request_payment_token", request.paymentMethodToken)
        logger.add("request_description", request.description)
        logger.add("request_idempotency_key", request.idempotencyKey)
        logger.add("request_customer_email", request.email)

        val tx = service.donate(request)
        logger.add("response_transaction_id", tx.id)
        logger.add("response_transaction_status", tx.status)

        return CreateDonationResponse(
            transactionId = tx.id ?: "",
            status = tx.status.name,
        )
    }
}
