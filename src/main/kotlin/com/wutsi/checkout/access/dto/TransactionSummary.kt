package com.wutsi.checkout.access.dto

import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

public data class TransactionSummary(
    public val id: String = "",
    public val customerId: Long? = null,
    public val businessId: Long = 0,
    public val type: String = "",
    public val description: String? = null,
    public val amount: Long = 0,
    public val fees: Long = 0,
    public val gatewayFees: Long = 0,
    public val net: Long = 0,
    public val currency: String = "",
    public val status: String = "",
    public val orderId: String? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    public val paymentMethod: PaymentMethodSummary = PaymentMethodSummary()
)
