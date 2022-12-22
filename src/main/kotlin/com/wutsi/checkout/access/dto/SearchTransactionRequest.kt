package com.wutsi.checkout.access.dto

public data class SearchTransactionRequest(
    public val customerAccountId: Long? = null,
    public val businessId: Long? = null,
    public val type: String? = null,
    public val status: List<String> = emptyList(),
    public val orderId: String? = null,
    public val limit: Int = 100,
    public val offset: Int = 0,
)
