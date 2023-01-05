package com.wutsi.checkout.access.entity

import com.wutsi.enums.DiscountType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "T_ORDER_DISCOUNT")
data class OrderDiscountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val discountId: Long = -1,
    val name: String = "",
    val amount: Long = 0L,
    val type: DiscountType = DiscountType.UNKNOWN,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_fk")
    val order: OrderEntity = OrderEntity(),
)
