package com.wutsi.checkout.access.dto

import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class SearchAccountRequest(
    public val ids: List<Long> = emptyList(),
    public val phoneNumber: String? = null,
    public val status: String? = null,
    public val business: Boolean? = null,
    public val hasStore: Boolean? = null,
    public val limit: Int = 30,
    public val offset: Int = 0,
    public val sortBy: String? = null
)
