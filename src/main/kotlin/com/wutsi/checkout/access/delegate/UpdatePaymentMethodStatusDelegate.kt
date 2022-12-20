package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.UpdatePaymentMethodStatusRequest
import com.wutsi.checkout.access.service.PaymentMethodService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdatePaymentMethodStatusDelegate(
    private val service: PaymentMethodService,
    private val logger: KVLogger,
) {
    @Transactional
    fun invoke(token: String, request: UpdatePaymentMethodStatusRequest) {
        logger.add("request_status", request.status)
        service.updateStatus(token, request)
    }
}
