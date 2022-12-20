package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String

public data class PaymentMethod(
    public val accountId: Long = 0,
    public val token: String = "",
    public val type: String = "",
    public val ownerName: String = "",
    public val number: String = "",
    public val country: String = "",
    public val status: String = "",
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val deactivated: OffsetDateTime? = null,
    public val provider: PaymentProviderSummary = PaymentProviderSummary(),
)
