package com.wutsi.checkout.access.dto

import kotlin.String

public data class SearchPaymentProviderRequest(
    public val country: String? = null,
    public val number: String? = null,
    public val type: String? = null,
)
