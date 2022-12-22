package com.wutsi.checkout.access.entity

import com.wutsi.enums.KpiType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "T_KPI")
data class KpiEntity(
    @Id
    val id: String? = null,

    val businessId: Long = -1,
    val productId: Long = -1,
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,

    val type: KpiType = KpiType.UNKNOWN,
    val value: Long = 0,
    val created: Date = Date(),
)
