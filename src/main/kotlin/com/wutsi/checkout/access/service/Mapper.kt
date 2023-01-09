package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dto.Business
import com.wutsi.checkout.access.dto.BusinessSummary
import com.wutsi.checkout.access.dto.Discount
import com.wutsi.checkout.access.dto.Order
import com.wutsi.checkout.access.dto.OrderItem
import com.wutsi.checkout.access.dto.OrderSummary
import com.wutsi.checkout.access.dto.PaymentMethod
import com.wutsi.checkout.access.dto.PaymentMethodSummary
import com.wutsi.checkout.access.dto.PaymentProviderSummary
import com.wutsi.checkout.access.dto.SalesKpiSummary
import com.wutsi.checkout.access.dto.Transaction
import com.wutsi.checkout.access.dto.TransactionSummary
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.entity.OrderDiscountEntity
import com.wutsi.checkout.access.entity.OrderEntity
import com.wutsi.checkout.access.entity.OrderItemDiscountEntity
import com.wutsi.checkout.access.entity.OrderItemEntity
import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.entity.PaymentProviderEntity
import com.wutsi.checkout.access.entity.SalesKpiEntity
import com.wutsi.checkout.access.entity.TransactionEntity
import java.time.ZoneOffset
import kotlin.math.max

object Mapper {
    fun toKpiSales(kpi: SalesKpiEntity) = SalesKpiSummary(
        date = kpi.date.toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
        totalOrders = kpi.totalOrders,
        totalUnits = kpi.totalUnits,
        totalValue = kpi.totalValue,
        totalViews = kpi.totalViews,
    )

    fun toBusiness(business: BusinessEntity, service: BusinessService) = Business(
        id = business.id ?: -1,
        accountId = business.accountId,
        status = business.status.name,
        balance = business.balance,
        currency = business.currency,
        country = business.country,
        created = business.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = business.updated.toInstant().atOffset(ZoneOffset.UTC),
        deactivated = business.deactivated?.toInstant()?.atOffset(ZoneOffset.UTC),
        totalOrders = business.totalOrders,
        totalSales = business.totalSales,
        totalViews = business.totalViews,
        cashoutBalance = service.computeCashoutBalance(business),
    )

    fun toBusinessSummary(business: BusinessEntity) = BusinessSummary(
        id = business.id ?: -1,
        accountId = business.accountId,
        status = business.status.name,
        balance = business.balance,
        currency = business.currency,
        country = business.country,
        created = business.created.toInstant().atOffset(ZoneOffset.UTC),
    )

    fun toOrder(order: OrderEntity) = Order(
        id = order.id ?: "",
        shortId = order.getShortId(),
        currency = order.currency,
        customerEmail = order.customerEmail,
        customerName = order.customerName,
        customerAccountId = order.customerAccountId,
        deviceId = order.deviceId,
        deviceType = order.deviceType?.name,
        channelType = order.channelType?.name,
        notes = order.notes,
        business = toBusinessSummary(order.business),
        totalPrice = order.totalPrice,
        totalPaid = order.totalPaid,
        balance = max(0, order.totalPrice - order.totalPaid),
        totalDiscount = order.totalDiscount,
        subTotalPrice = order.subTotalPrice,
        status = order.status.name,
        created = order.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = order.updated.toInstant().atOffset(ZoneOffset.UTC),
        cancelled = order.cancelled?.toInstant()?.atOffset(ZoneOffset.UTC),
        closed = order.closed?.toInstant()?.atOffset(ZoneOffset.UTC),
        expired = order.expired?.toInstant()?.atOffset(ZoneOffset.UTC),
        expires = order.expires.toInstant().atOffset(ZoneOffset.UTC),
        cancellationReason = order.cancellationReason,
        items = order.items.map { toOrderItem(it) },
        discounts = order.discounts.map { toOrderDiscount(order, it) },
        itemCount = order.itemCount,
        transactions = order.transaactions.map { toTransactionSummary(it) },
    )

    fun toOrderSummary(order: OrderEntity) = OrderSummary(
        id = order.id ?: "",
        shortId = order.getShortId(),
        currency = order.currency,
        customerEmail = order.customerEmail,
        customerName = order.customerName,
        customerAccountId = order.customerAccountId,
        businessId = order.business.id ?: -1,
        totalPrice = order.totalPrice,
        balance = max(0, order.totalPrice - order.totalPaid),
        status = order.status.name,
        created = order.created.toInstant().atOffset(ZoneOffset.UTC),
        itemCount = order.itemCount,
        productPictureUrls = listOfNotNull(
            order.productPictureUrl1,
            order.productPictureUrl2,
            order.productPictureUrl3,
        ),
    )

    private fun toOrderItem(item: OrderItemEntity) = OrderItem(
        productId = item.productId,
        productType = item.productType.name,
        title = item.title,
        quantity = item.quantity,
        pictureUrl = item.pictureUrl,
        unitPrice = item.unitPrice,
        totalPrice = item.totalPrice,
        totalDiscount = item.totalDiscount,
        subTotalPrice = item.subTotalPrice,
        discounts = item.discounts.map { toOrderItemDiscount(item, it) },
    )

    private fun toOrderDiscount(order: OrderEntity, discount: OrderDiscountEntity) = Discount(
        discountId = discount.discountId,
        name = discount.name,
        type = discount.type.name,
        amount = discount.amount,
    )

    private fun toOrderItemDiscount(item: OrderItemEntity, discount: OrderItemDiscountEntity) = Discount(
        discountId = discount.discountId,
        name = discount.name,
        type = discount.type.name,
        amount = discount.amount,
    )

    fun toTransaction(tx: TransactionEntity) = Transaction(
        id = tx.id ?: "",
        financialTransactionId = tx.financialTransactionId,
        amount = tx.amount,
        business = toBusinessSummary(tx.business),
        status = tx.status.name,
        currency = tx.currency,
        created = tx.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = tx.updated.toInstant().atOffset(ZoneOffset.UTC),
        type = tx.type.name,
        description = tx.description,
        errorCode = tx.errorCode,
        fees = tx.fees,
        orderId = tx.order?.id,
        gatewayFees = tx.gatewayFees,
        supplierErrorCode = tx.supplierErrorCode,
        supplierErrorMessage = tx.supplierErrorMessage,
        net = tx.net,
        gatewayTransactionId = tx.gatewayTransactionId,
        customerAccountId = tx.customerAccountId,
        gatewayType = tx.gatewayType.name,
        email = tx.email,
        paymentMethod = toPaymentMethodSummary(tx),
    )

    fun toTransactionSummary(tx: TransactionEntity) = TransactionSummary(
        id = tx.id ?: "",
        amount = tx.amount,
        businessId = tx.business.id ?: -1,
        status = tx.status.name,
        currency = tx.currency,
        created = tx.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = tx.updated.toInstant().atOffset(ZoneOffset.UTC),
        type = tx.type.name,
        fees = tx.fees,
        orderId = tx.order?.id,
        gatewayFees = tx.gatewayFees,
        net = tx.net,
        customerAccountId = tx.customerAccountId,
        paymentMethod = toPaymentMethodSummary(tx),
    )

    private fun toPaymentMethodSummary(tx: TransactionEntity) = PaymentMethodSummary(
        token = tx.paymentMethod?.token ?: "",
        accountId = tx.paymentMethod?.accountId ?: -1,
        status = tx.paymentMethod?.status?.name ?: "",

        number = tx.paymentMethodNumber,
        type = tx.paymentMethodType.name,
        ownerName = tx.paymentMethodOwnerName,
        provider = toPaymentProviderSummary(tx.paymentProvider),
    )

    fun toPaymentProviderSummary(provider: PaymentProviderEntity) = PaymentProviderSummary(
        id = provider.id ?: -1,
        code = provider.code,
        name = provider.name,
        logoUrl = provider.logoUrl,
        type = provider.type.name,
    )

    fun toPaymentMethod(payment: PaymentMethodEntity) = PaymentMethod(
        accountId = payment.accountId,
        token = payment.token,
        type = payment.type.name,
        status = payment.status.name,
        number = payment.number,
        ownerName = payment.ownerName,
        country = payment.country,
        created = payment.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = payment.updated.toInstant().atOffset(ZoneOffset.UTC),
        deactivated = payment.deactivated?.toInstant()?.atOffset(ZoneOffset.UTC),
        provider = toPaymentProviderSummary(payment.provider),
    )

    fun toPaymentMethodSummary(payment: PaymentMethodEntity) = PaymentMethodSummary(
        accountId = payment.accountId,
        token = payment.token,
        type = payment.type.name,
        status = payment.status.name,
        number = payment.number,
        created = payment.created.toInstant().atOffset(ZoneOffset.UTC),
        provider = toPaymentProviderSummary(payment.provider),
    )
}
