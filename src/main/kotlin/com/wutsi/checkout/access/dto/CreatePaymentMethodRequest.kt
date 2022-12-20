package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.Long
import kotlin.String

public data class CreatePaymentMethodRequest(
    public val accountId: Long = 0,
    public val providerId: Long = 0,
    @get:NotBlank
    public val type: String = "",
    @get:NotBlank
    public val number: String = "",
    public val country: String = "",
    @get:NotBlank
    @get:Size(max = 100)
    public val ownerName: String = "",
)
