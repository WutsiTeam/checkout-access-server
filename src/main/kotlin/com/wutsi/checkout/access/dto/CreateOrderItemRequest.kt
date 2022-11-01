package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class CreateOrderItemRequest(
    public val offerId: Long = 0,
    @get:NotBlank
    public val offerType: String = "",
    @get:NotBlank
    public val title: String = "",
    public val quantity: Int = 0,
    public val pictureUrl: String? = null,
    public val price: Long = 0,
    public val discounts: List<CreateOrderDiscountRequest> = emptyList()
)
