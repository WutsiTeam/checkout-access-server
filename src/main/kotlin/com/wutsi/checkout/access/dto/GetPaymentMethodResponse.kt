package com.wutsi.checkout.access.dto

public data class GetPaymentMethodResponse(
    public val paymentMethod: PaymentMethod = PaymentMethod(),
)
