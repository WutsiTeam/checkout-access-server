package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class AccountSummary(
    public val id: Long = 0,
    public val pictureUrl: String? = null,
    public val status: String = "",
    public val displayName: String? = null,
    public val language: String = "",
    public val country: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    public val superUser: Boolean = false,
    public val business: Boolean = false,
    public val categoryId: Long? = null,
    public val cityId: Long? = null
)
