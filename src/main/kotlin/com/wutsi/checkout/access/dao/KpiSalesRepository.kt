package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.entity.CustomerEntity
import com.wutsi.checkout.access.entity.KpiSalesEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.Optional

@Repository
interface KpiSalesRepository : CrudRepository<KpiSalesEntity, Long> {
    fun findByBusinessAndCustomerAndProductIdAndDate(
        business: BusinessEntity,
        customer: CustomerEntity,
        productId: Long,
        date: Date,
    ): Optional<KpiSalesEntity>
}
