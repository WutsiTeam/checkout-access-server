package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.CreatePaymentMethodResponse
import com.wutsi.checkout.access.service.PaymentMethodService
import org.springframework.stereotype.Service

@Service
class CreatePaymentMethodDelegate(private val service: PaymentMethodService) {
    fun invoke(request: CreatePaymentMethodRequest): CreatePaymentMethodResponse {
        val paymentMethod = service.create(request)
        return CreatePaymentMethodResponse(
            paymentMethodToken = paymentMethod.token
        )
    }
}
