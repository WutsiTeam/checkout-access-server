package com.wutsi.checkout.access.entity

import com.wutsi.enums.BusinessStatus
import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "T_BUSINESS")
data class BusinessEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val accountId: Long = -1,
    var status: BusinessStatus = BusinessStatus.UNKNOWN,
    var balance: Long = 0,
    val country: String = "",
    val currency: String = "",

    val created: Date = Date(),
    var updated: Date = Date(),
    var deactivated: Date? = null,
    val totalOrders: Long = 0,
    val totalSales: Long = 0,
    val totalViews: Long = 0,
)
