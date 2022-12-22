package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.KpiEntity
import com.wutsi.enums.KpiType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface KpiRepository : CrudRepository<KpiEntity, Long> {
    fun findByBusinessIdAndTypeAndYearAndMonthAndDay(
        businessId: Long,
        type: KpiType,
        year: Int,
        month: Int,
        day: Int,
    ): Optional<KpiEntity>

    fun findByProductIdAndTypeAndYearAndMonthAndDay(
        productId: Long,
        type: KpiType,
        year: Int,
        month: Int,
        day: Int,
    ): Optional<KpiEntity>
}
