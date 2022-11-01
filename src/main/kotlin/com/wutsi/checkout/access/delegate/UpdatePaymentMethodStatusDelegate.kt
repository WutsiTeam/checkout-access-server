package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.UpdatePaymentMethodStatusRequest
import com.wutsi.checkout.access.service.PaymentMethodService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdatePaymentMethodStatusDelegate(private val service: PaymentMethodService) {
    @Transactional
    fun invoke(token: String, request: UpdatePaymentMethodStatusRequest) {
        service.updateStatus(token, request)
    }
}
