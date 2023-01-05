package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank

public data class CreateOrderDiscountRequest(
    public val discountId: Long = 0,
    @get:NotBlank
    public val name: String = "",
    public val amount: Long = 0,
    @get:NotBlank
    public val type: String = "",
)
