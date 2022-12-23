package com.wutsi.checkout.access.dto

import org.springframework.format.`annotation`.DateTimeFormat
import java.time.LocalDate
import kotlin.Boolean
import kotlin.Long

public data class SearchSalesKpiRequest(
    public val businessId: Long? = null,
    public val productId: Long? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd")
    public val fromDate: LocalDate? = null,
    @get:DateTimeFormat(pattern = "yyyy-MM-dd")
    public val toDate: LocalDate? = null,
    public val aggregate: Boolean = false,
)
