package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class CreateOrderRequest(
    public val deviceId: String? = null,
    public val deviceIp: String? = null,
    public val deviceType: String? = null,
    public val channelType: String? = null,
    public val storeId: Long = 0,
    public val notes: String? = null,
    @get:NotBlank
    public val currency: String = "",
    public val customerId: Long = 0,
    public val customerName: String = "",
    public val customerEmail: String? = null,
    @get:NotNull
    @get:NotEmpty
    public val items: List<CreateOrderItemRequest> = emptyList(),
    public val discounts: List<CreateOrderDiscountRequest> = emptyList()
)
