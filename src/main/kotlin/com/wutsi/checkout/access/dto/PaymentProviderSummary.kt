package com.wutsi.checkout.access.dto

public data class PaymentProviderSummary(
    public val id: Long = -1,
    public val code: String = "",
    public val name: String = "",
    public val logoUrl: String = "",
    public val type: String = ""
)
