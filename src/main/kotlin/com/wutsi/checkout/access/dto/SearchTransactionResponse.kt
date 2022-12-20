package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class SearchTransactionResponse(
    public val transactions: List<TransactionSummary> = emptyList(),
)
