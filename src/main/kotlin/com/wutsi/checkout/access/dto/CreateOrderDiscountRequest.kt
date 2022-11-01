package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank
import kotlin.Long
import kotlin.String

public data class CreateOrderDiscountRequest(
    @get:NotBlank
    public val code: String = "",
    public val amount: Long = 0,
    @get:NotBlank
    public val type: String = ""
)
