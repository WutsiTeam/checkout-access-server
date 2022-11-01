package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.OrderItemDiscountEntity
import com.wutsi.checkout.access.entity.OrderItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemDiscountRepository : CrudRepository<OrderItemDiscountEntity, Long> {
    fun findByOrderItem(order: OrderItemEntity): List<OrderItemDiscountEntity>
}
