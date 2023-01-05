package com.wutsi.checkout.access.dto

public data class Discount(
    public val discountId: Long = 0,
    public val name: String = "",
    public val amount: Long = 0,
    public val rate: Int = 0,
    public val type: String = "",
)
