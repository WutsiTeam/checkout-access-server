package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.LocalDate
import kotlin.Long

public data class SalesKpiSummary(
    @get:DateTimeFormat(pattern = "yyyy-MM-dd")
    public val date: LocalDate = LocalDate.now(),
    public val totalOrders: Long = 0,
    public val totalUnits: Long = 0,
    public val totalValue: Long = 0,
    public val totalViews: Long = 0,
)
