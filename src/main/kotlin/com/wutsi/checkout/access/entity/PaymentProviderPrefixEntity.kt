package com.wutsi.checkout.access.entity

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "T_PAYMENT_PROVIDER_PREFIX")
data class PaymentProviderPrefixEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val country: String = "",
    val numberPrefix: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_provider_fk")
    val provider: PaymentProviderEntity = PaymentProviderEntity(),
)
