package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class SearchPaymentMethodResponse(
    public val paymentMethods: List<PaymentMethodSummary> = emptyList(),
)
