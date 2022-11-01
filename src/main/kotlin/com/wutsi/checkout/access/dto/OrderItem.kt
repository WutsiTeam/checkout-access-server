package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class OrderItem(
    public val offerId: Long = 0,
    public val offerType: String = "",
    public val title: String = "",
    public val quantity: Int = 0,
    public val pictureUrl: String? = null,
    public val price: Long = 0,
    public val discounts: List<Discount> = emptyList()
)
