package com.wutsi.checkout.access.dto

import javax.validation.constraints.Size
import kotlin.String

public data class UpdateAccountRequest(
    @get:Size(max = 2)
    public val language: String = "en",
    @get:Size(max = 2)
    public val country: String = "US",
    public val displayName: String? = null
)
