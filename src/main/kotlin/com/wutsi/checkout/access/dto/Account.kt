package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class Account(
    public val id: Long = 0,
    public val email: String? = null,
    public val phone: Phone = Phone(),
    public val pictureUrl: String? = null,
    public val status: String = "",
    public val displayName: String? = null,
    public val language: String = "",
    public val country: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val suspended: OffsetDateTime? = null,
    public val superUser: Boolean = false,
    public val business: Boolean = false,
    public val biography: String? = null,
    public val website: String? = null,
    public val whatsapp: String? = null,
    public val street: String? = null,
    public val cityId: Long? = null,
    public val timezoneId: String? = null,
    public val category: Category = Category(),
    public val hasStore: Boolean = false,
    public val facebookId: String? = null,
    public val instagramId: String? = null,
    public val twitterId: String? = null,
    public val fcmToken: String? = null
)
