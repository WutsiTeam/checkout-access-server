package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.String

public data class Transaction(
    public val id: String = "",
    public val business: BusinessSummary = BusinessSummary(),
    public val customerAccountId: Long? = null,
    public val type: String = "",
    public val description: String? = null,
    public val amount: Long = 0,
    public val fees: Long = 0,
    public val gatewayFees: Long = 0,
    public val net: Long = 0,
    public val currency: String = "",
    public val status: String = "",
    public val gatewayTransactionId: String? = null,
    public val financialTransactionId: String? = null,
    public val errorCode: String? = null,
    public val supplierErrorCode: String? = null,
    public val supplierErrorMessage: String? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val created: OffsetDateTime = OffsetDateTime.now(),
    @get:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public val updated: OffsetDateTime = OffsetDateTime.now(),
    public val orderId: String? = null,
    public val gatewayType: String = "",
    public val email: String? = null,
    public val paymentMethod: PaymentMethodSummary = PaymentMethodSummary(),
)
