package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public data class Order(
    public val id: String = "",
    public val shortId: String = "",
    public val deviceId: String? = null,
    public val deviceIp: String? = null,
    public val deviceType: String? = null,
    public val channelType: String? = null,
    public val storeId: Long = 0,
    public val status: String = "",
    public val subTotalPrice: Long = 0,
    public val totalDiscount: Double = 0.0,
    public val totalPrice: Double = 0.0,
    public val currency: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val cancelled: OffsetDateTime? = null,
    public val cancellationReason: String? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val closed: OffsetDateTime? = null,
    public val notes: String = "",
    public val customerId: Long = 0,
    public val customerName: String = "",
    public val customerEmail: String? = null,
    public val items: List<OrderItem> = emptyList(),
    public val discounts: List<Discount> = emptyList()
)
