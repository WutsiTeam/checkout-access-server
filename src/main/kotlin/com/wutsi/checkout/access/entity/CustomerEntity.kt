package com.wutsi.checkout.access.entity

import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "T_CUSTOMER")
data class CustomerEntity(
    @Id
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_fk")
    val business: BusinessEntity = BusinessEntity(),

    val email: String = "",
    val created: Date = Date(),
)
