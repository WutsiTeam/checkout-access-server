package com.wutsi.checkout.access.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class CreateOrderItemRequest(
    public val productId: Long = 0,
    @get:NotBlank
    public val productType: String = "",
    @get:NotBlank
    @get:Size(max = 100)
    public val title: String = "",
    public val quantity: Int = 0,
    public val pictureUrl: String? = null,
    @get:Min(0)
    public val unitPrice: Long = 0,
    public val discounts: List<CreateOrderDiscountRequest> = emptyList(),
)
