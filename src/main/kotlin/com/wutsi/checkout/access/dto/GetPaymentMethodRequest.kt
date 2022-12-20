package com.wutsi.checkout.access.dto

public data class GetPaymentMethodRequest(
    public val paymentMethod: PaymentMethod = PaymentMethod(),
)
