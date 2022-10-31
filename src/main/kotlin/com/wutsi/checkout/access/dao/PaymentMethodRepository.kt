package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.enums.PaymentMethodStatus
import com.wutsi.checkout.access.enums.PaymentMethodType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PaymentMethodRepository : CrudRepository<PaymentMethodEntity, Long> {
    fun findByToken(token: String): Optional<PaymentMethodEntity>
    fun findByTypeAndNumberAndStatus(
        type: PaymentMethodType,
        number: String,
        status: PaymentMethodStatus
    ): List<PaymentMethodEntity>
}
