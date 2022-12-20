package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long
import kotlin.String

public data class SearchPaymentMethodRequest(
    public val accountId: Long = 0,
    public val status: String? = null,
    public val limit: Int = 100,
    public val offset: Int = 0,
)
