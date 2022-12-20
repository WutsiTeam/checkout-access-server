package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class SearchPaymentProviderResponse(
    public val paymentProviders: List<PaymentProviderSummary> = emptyList(),
)
