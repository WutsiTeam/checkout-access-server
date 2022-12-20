package com.wutsi.checkout.access.dto

import kotlin.String

public data class UpdateOrderStatusRequest(
    public val status: String = "",
    public val reason: String? = null,
)
