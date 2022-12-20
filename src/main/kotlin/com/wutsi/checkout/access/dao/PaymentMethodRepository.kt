package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PaymentMethodRepository : CrudRepository<PaymentMethodEntity, Long> {
    fun findByToken(token: String): Optional<PaymentMethodEntity>

    fun findByTypeAndNumberAndStatus(
        type: PaymentMethodType,
        number: String,
        status: PaymentMethodStatus,
    ): List<PaymentMethodEntity>

    fun findByAccountId(accountId: Long, pagination: Pageable): List<PaymentMethodEntity>
    fun findByAccountIdAndStatus(
        accountId: Long,
        status: PaymentMethodStatus,
        pagination: Pageable,
    ): List<PaymentMethodEntity>
}
