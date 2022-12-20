package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class OrderItem(
    public val productId: Long = 0,
    public val productType: String = "",
    public val title: String = "",
    public val quantity: Int = 0,
    public val pictureUrl: String? = null,
    public val unitPrice: Long = 0,
    public val subTotalPrice: Long = 0,
    public val totalDiscount: Long = 0,
    public val totalPrice: Long = 0,
    public val discounts: List<Discount> = emptyList(),
)
