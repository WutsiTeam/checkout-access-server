package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.OrderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : CrudRepository<OrderEntity, String>
