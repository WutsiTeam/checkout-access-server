package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import javax.validation.constraints.Size
import kotlin.Long
import kotlin.String

public data class Business(
    public val id: Long = 0,
    public val accountId: Long = 0,
    public val balance: Long = 0,
    @get:Size(
        min = 3,
        max = 3
    )
    public val currency: String = "",
    public val status: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val suspended: OffsetDateTime? = null
)
