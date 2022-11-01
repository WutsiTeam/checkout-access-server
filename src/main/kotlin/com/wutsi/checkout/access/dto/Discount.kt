package com.wutsi.checkout.access.dto

import kotlin.Int
import kotlin.Long
import kotlin.String

public data class Discount(
    public val code: String = "",
    public val amount: Long = 0,
    public val rate: Int = 0,
    public val type: String = ""
)
