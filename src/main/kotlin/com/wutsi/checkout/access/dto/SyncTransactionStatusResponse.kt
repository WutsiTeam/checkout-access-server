package com.wutsi.checkout.access.dto

import kotlin.String

public data class SyncTransactionStatusResponse(
    public val transactionId: String = "",
    public val status: String = ""
)
