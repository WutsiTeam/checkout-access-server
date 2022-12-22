package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class SearchTransactionRequest(
    public val customerAccountId: Long? = null,
    public val businessId: Long? = null,
    public val type: String? = null,
    public val status: List<String> = emptyList(),
    public val orderId: String? = null,
    public val limit: Int = 100,
    public val offset: Int = 0,
)
