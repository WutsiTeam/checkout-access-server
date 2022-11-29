package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.OrderEntity
import com.wutsi.checkout.access.entity.TransactionEntity
import com.wutsi.platform.payment.core.Status
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TransactionRepository : CrudRepository<TransactionEntity, String> {
    fun findByIdempotencyKey(idempotencyKey: String): Optional<TransactionEntity>
    fun findByOrderAndStatus(order: OrderEntity, status: Status): List<TransactionEntity>
}
