package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class OrderSummary(
    public val id: String = "",
    public val shortId: String = "",
    public val businessId: Long = 0,
    public val status: String = "",
    public val totalPrice: Long = 0,
    public val balance: Long = 0,
    public val currency: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    public val customerAccountId: Long? = null,
    public val customerName: String = "",
    public val customerEmail: String = "",
    public val itemCount: Int = 0,
    public val productPictureUrls: List<String> = emptyList(),
)
