package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.entity.SalesKpiEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.Optional

@Repository
interface SalesKpiRepository : CrudRepository<SalesKpiEntity, Long> {
    fun findByBusinessAndProductIdAndDate(
        business: BusinessEntity,
        productId: Long,
        date: Date,
    ): Optional<SalesKpiEntity>
}
