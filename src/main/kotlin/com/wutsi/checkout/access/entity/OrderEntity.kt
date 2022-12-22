package com.wutsi.checkout.access.entity

import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.OrderStatus
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "T_ORDER")
data class OrderEntity(
    @Id
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_fk")
    val business: BusinessEntity = BusinessEntity(),

    val customerAccountId: Long? = null,
    val customerName: String = "",
    val customerEmail: String = "",
    val deviceId: String? = null,
    val deviceType: DeviceType? = null,
    val channelType: ChannelType? = null,
    var status: OrderStatus = OrderStatus.UNKNOWN,
    var totalPrice: Long = 0L,
    val subTotalPrice: Long = 0L,
    val totalDiscount: Long = 0L,
    var totalPaid: Long = 0L,
    val currency: String = "",
    val notes: String? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    var items: List<OrderItemEntity> = emptyList(),

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    val discounts: List<OrderDiscountEntity> = emptyList(),

    val created: Date = Date(),
    var updated: Date = Date(),
    var cancelled: Date? = null,
    var expired: Date? = null,
    var closed: Date? = null,
    var cancellationReason: String? = null,
    val expires: Date = Date(),
    val itemCount: Int = 0,
    val productPictureUrl1: String? = null,
    val productPictureUrl2: String? = null,
    val productPictureUrl3: String? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    val transaactions: List<TransactionEntity> = emptyList(),
) {
    fun getShortId(): String =
        id?.takeLast(4)?.uppercase() ?: ""
}
