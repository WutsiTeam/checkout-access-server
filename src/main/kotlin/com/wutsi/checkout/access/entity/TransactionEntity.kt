package com.wutsi.checkout.access.entity

import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.platform.payment.GatewayType
import com.wutsi.platform.payment.core.Status
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "T_TRANSACTION")
data class TransactionEntity(
    @Id
    val id: String? = null,
    val idempotencyKey: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_fk")
    val business: BusinessEntity = BusinessEntity(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_fk")
    val order: OrderEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_fk")
    val paymentMethod: PaymentMethodEntity? = null,

    val type: TransactionType = TransactionType.UNKNOWN,
    var status: Status = Status.UNKNOWN,
    val gatewayType: GatewayType = GatewayType.UNKNOWN,

    val customerAccountId: Long? = null,
    var email: String? = null,
    val description: String? = null,
    var amount: Long = 0L,
    var fees: Long = 0L,
    var net: Long = 0L,
    val currency: String = "",
    var gatewayTransactionId: String? = null,
    var financialTransactionId: String? = null,
    var errorCode: String? = null,
    var supplierErrorCode: String? = null,
    var gatewayFees: Long = 0,
    var supplierErrorMessage: String? = null,

    val created: Date = Date(),
    var updated: Date = Date(),

    val paymentMethodNumber: String = "",
    val paymentMethodCountry: String? = null,
    val paymentMethodOwnerName: String = "",
    val paymentMethodType: PaymentMethodType = PaymentMethodType.UNKNOWN,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_provider_fk")
    val paymentProvider: PaymentProviderEntity = PaymentProviderEntity(),
)
