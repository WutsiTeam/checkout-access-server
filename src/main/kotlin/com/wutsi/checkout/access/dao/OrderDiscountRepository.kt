package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.OrderDiscountEntity
import com.wutsi.checkout.access.entity.OrderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderDiscountRepository : CrudRepository<OrderDiscountEntity, Long> {
    fun findByOrder(order: OrderEntity): List<OrderDiscountEntity>
}
