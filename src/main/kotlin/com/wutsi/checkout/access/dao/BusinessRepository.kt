package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.enums.BusinessStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BusinessRepository : CrudRepository<BusinessEntity, Long> {
    fun findByAccountIdAndStatusNot(accountId: Long, status: BusinessStatus): List<BusinessEntity>
}
