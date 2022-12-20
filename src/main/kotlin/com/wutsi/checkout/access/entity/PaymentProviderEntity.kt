package com.wutsi.checkout.access.entity

import com.wutsi.enums.PaymentMethodType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "T_PAYMENT_PROVIDER")
data class PaymentProviderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val code: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val type: PaymentMethodType = PaymentMethodType.UNKNOWN,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "provider")
    val prefixes: List<PaymentProviderPrefixEntity> = emptyList(),
)
