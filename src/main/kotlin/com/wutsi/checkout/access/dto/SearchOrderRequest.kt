package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long

public data class SearchOrderRequest(
    public val storeId: Long? = null,
    public val customerId: Long? = null,
    public val limit: Int = 100,
    public val offset: Int = 0
)
