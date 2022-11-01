package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.OrderEntity
import com.wutsi.checkout.access.entity.OrderItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : CrudRepository<OrderItemEntity, Long> {
    fun findByOrder(order: OrderEntity): List<OrderItemEntity>
}
