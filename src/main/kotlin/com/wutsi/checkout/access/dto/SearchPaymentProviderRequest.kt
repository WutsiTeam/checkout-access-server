package com.wutsi.checkout.access.dto

import javax.validation.constraints.NotBlank
import kotlin.String

public data class SearchPaymentProviderRequest(
    @get:NotBlank
    public val country: String = "",
    public val number: String? = null,
    public val type: String? = null
)
