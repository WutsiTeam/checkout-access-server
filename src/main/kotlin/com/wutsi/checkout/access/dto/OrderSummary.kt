package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String

public data class OrderSummary(
    public val id: String = "",
    public val shortId: String = "",
    public val businessId: Long = 0,
    public val status: String = "",
    public val subTotalPrice: Long = 0,
    public val totalDiscount: Long = 0,
    public val totalPrice: Long = 0,
    public val totalPaid: Long = 0,
    public val balance: Long = 0,
    public val currency: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val cancelled: OffsetDateTime? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val closed: OffsetDateTime? = null,
    public val customerId: Long? = null,
    public val customerName: String = "",
    public val customerEmail: String = ""
)
