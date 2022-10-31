package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class SearchAccountResponse(
    public val accounts: List<AccountSummary> = emptyList()
)
