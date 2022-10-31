package com.wutsi.checkout.access.entity

import com.wutsi.checkout.access.enums.PaymentMethodStatus
import com.wutsi.checkout.access.enums.PaymentMethodType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "T_PAYMENT_METHOD")
data class PaymentMethodEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val accountId: Long = -1,
    val token: String = "",
    var number: String = "",
    var country: String = "",
    var ownerName: String = "",

    @Enumerated
    val type: PaymentMethodType = PaymentMethodType.UNKNOWN,

    @Enumerated
    var status: PaymentMethodStatus = PaymentMethodStatus.UNKNOWN,

    val created: Date = Date(),
    val updated: Date = Date(),
    var deactivated: Date? = null
)
