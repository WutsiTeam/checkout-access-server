package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class SearchSalesKpiResponse(
    public val kpis: List<SalesKpiSummary> = emptyList(),
)
