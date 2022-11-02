package com.wutsi.checkout.access.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.Long
import kotlin.String

public data class CreateCashoutRequest(
    @get:NotBlank
    public val paymentMethodToken: String = "",
    public val businessId: Long = 0,
    @get:Min(0)
    public val amount: Long = 0,
    @get:NotBlank
    @get:Size(max = 36)
    public val idempotencyKey: String = ""
)
