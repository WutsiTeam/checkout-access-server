package com.wutsi.checkout.access.dto

import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime
import javax.validation.constraints.Size

public data class Order(
    public val id: String = "",
    public val shortId: String = "",
    public val deviceId: String? = null,
    public val reservationId: Long? = null,
    public val deviceType: String? = null,
    public val channelType: String? = null,
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
    public val cancellationReason: String? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val closed: OffsetDateTime? = null,
    public val notes: String? = null,
    public val customerId: Long? = null,
    public val customerName: String = "",
    @get:Size(max = 100)
    public val customerEmail: String = "",
    public val items: List<OrderItem> = emptyList(),
    public val discounts: List<Discount> = emptyList()
)
