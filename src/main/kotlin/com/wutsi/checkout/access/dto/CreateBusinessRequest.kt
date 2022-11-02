package com.wutsi.checkout.access.dto

import javax.validation.constraints.Size
import kotlin.Long
import kotlin.String

public data class CreateBusinessRequest(
    public val accountId: Long = 0,
    @get:Size(
        min = 3,
        max = 3
    )
    public val currency: String = ""
)
