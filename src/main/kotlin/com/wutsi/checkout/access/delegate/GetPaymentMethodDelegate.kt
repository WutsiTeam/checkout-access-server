package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.GetPaymentMethodResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.PaymentMethodService
import org.springframework.stereotype.Service

@Service
class GetPaymentMethodDelegate(
    private val service: PaymentMethodService,
) {
    fun invoke(token: String): GetPaymentMethodResponse {
        val payment = service.findByToken(token)
        return GetPaymentMethodResponse(
            paymentMethod = Mapper.toPaymentMethod(payment),
        )
    }
}
