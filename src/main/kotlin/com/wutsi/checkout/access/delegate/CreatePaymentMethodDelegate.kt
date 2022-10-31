package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.CreatePaymentMethodResponse
import com.wutsi.checkout.access.service.PaymentMethodService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class CreatePaymentMethodDelegate(
    private val service: PaymentMethodService,
    private val logger: KVLogger
) {
    fun invoke(request: CreatePaymentMethodRequest): CreatePaymentMethodResponse {
        logger.add("request_country", request.country)
        logger.add("request_owner_name", request.ownerName)
        logger.add("request_type", request.type)
        logger.add("request_account_id", request.accountId)
        logger.add("request_number", service.mask(request.number))

        val paymentMethod = service.create(request)
        return CreatePaymentMethodResponse(
            paymentMethodToken = paymentMethod.token
        )
    }
}
