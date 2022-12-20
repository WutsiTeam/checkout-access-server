package com.wutsi.checkout.access.entity

import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import java.util.Date
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
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
    var deactivated: Date? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_provider_fk")
    val provider: PaymentProviderEntity = PaymentProviderEntity(),
)
