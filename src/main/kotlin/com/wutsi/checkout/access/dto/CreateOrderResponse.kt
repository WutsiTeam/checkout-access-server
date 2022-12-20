package com.wutsi.checkout.access.dto

import kotlin.String

public data class CreateOrderResponse(
    public val orderId: String = "",
    public val orderStatus: String = "",
)
