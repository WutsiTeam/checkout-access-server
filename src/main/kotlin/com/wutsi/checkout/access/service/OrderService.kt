package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.OrderDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemRepository
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateOrderDiscountRequest
import com.wutsi.checkout.access.dto.CreateOrderItemRequest
import com.wutsi.checkout.access.dto.CreateOrderRequest
import com.wutsi.checkout.access.dto.Discount
import com.wutsi.checkout.access.dto.Order
import com.wutsi.checkout.access.dto.OrderItem
import com.wutsi.checkout.access.dto.OrderSummary
import com.wutsi.checkout.access.dto.SearchOrderRequest
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.entity.OrderDiscountEntity
import com.wutsi.checkout.access.entity.OrderEntity
import com.wutsi.checkout.access.entity.OrderItemDiscountEntity
import com.wutsi.checkout.access.entity.OrderItemEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.DiscountType
import com.wutsi.enums.OrderStatus
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.payment.core.Status
import org.springframework.stereotype.Service
import java.lang.Long.max
import java.time.ZoneOffset
import java.util.Date
import java.util.UUID
import javax.persistence.EntityManager
import javax.persistence.Query

@Service
class OrderService(
    private val dao: OrderRepository,
    private val itemDao: OrderItemRepository,
    private val discountDao: OrderDiscountRepository,
    private val itemDiscountDao: OrderItemDiscountRepository,
    private val transactionDao: TransactionRepository,
    private val em: EntityManager,
    private val tracingContext: TracingContext
) {
    fun create(business: BusinessEntity, request: CreateOrderRequest): OrderEntity {
        // Order
        val subTotalPrice = computeSubTotalPrice(request)
        val totalDiscount = computeTotalDiscount(request)
        val order = dao.save(
            OrderEntity(
                id = UUID.randomUUID().toString(),
                business = business,
                customerId = request.customerId,
                customerEmail = request.customerEmail,
                customerName = request.customerName,
                status = OrderStatus.OPENED,
                currency = request.currency,
                deviceId = tracingContext.deviceId(),
                deviceType = request.deviceType?.let { DeviceType.valueOf(it.uppercase()) },
                subTotalPrice = subTotalPrice,
                totalDiscount = totalDiscount,
                totalPrice = max(0L, subTotalPrice - totalDiscount),
                channelType = request.channelType?.let { ChannelType.valueOf(it.uppercase()) },
                notes = request.notes
            )
        )

        // Discount
        request.discounts.forEach {
            create(order, it)
        }

        // Items
        request.items.forEach {
            create(order, it)
        }
        return order
    }

    fun updateStatus(id: String, request: UpdateOrderStatusRequest) {
        val order = findById(id)
        val status = OrderStatus.valueOf(request.status.uppercase())
        if (status == order.status) {
            return
        }

        order.status = status
        when (status) {
            OrderStatus.CLOSED -> order.closed = Date()

            OrderStatus.CANCELLED -> {
                order.cancelled = Date()
                order.cancellationReason = request.reason
            }
            else -> BadRequestException(
                error = Error(
                    code = ErrorURN.STATUS_NOT_VALID.urn,
                    parameter = Parameter(
                        name = "status",
                        value = request.status,
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD
                    )
                )
            )
        }
        dao.save(order)
    }

    fun findById(id: String): OrderEntity =
        dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.ORDER_NOT_FOUND.urn,
                        parameter = Parameter(
                            name = "id",
                            value = id,
                            type = ParameterType.PARAMETER_TYPE_PATH
                        )
                    )
                )
            }

    fun updateBalance(id: String) {
        val order = findById(id)
        order.totalPaid = transactionDao.findByOrderIdAndStatus(id, Status.SUCCESSFUL)
            .filter { it.type == TransactionType.CHARGE }
            .sumOf { it.net }
        dao.save(order)
    }

    fun toOrder(order: OrderEntity) = Order(
        id = order.id ?: "",
        shortId = toShortId(order.id),
        currency = order.currency,
        customerEmail = order.customerEmail,
        customerName = order.customerName,
        customerId = order.customerId,
        deviceId = order.deviceId,
        deviceType = order.deviceType?.name,
        channelType = order.channelType?.name,
        notes = order.notes,
        businessId = order.business.id ?: -1,
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
        cancellationReason = order.cancellationReason,
        items = order.items.map { toOrderItem(it) },
        discounts = order.discounts.map { toOrderDiscount(order, it) }
    )

    fun toOrderSummary(order: OrderEntity) = OrderSummary(
        id = order.id ?: "",
        shortId = toShortId(order.id),
        currency = order.currency,
        customerEmail = order.customerEmail,
        customerName = order.customerName,
        customerId = order.customerId,
        businessId = order.business.id ?: -1,
        totalPrice = order.totalPrice,
        totalDiscount = order.totalDiscount,
        subTotalPrice = order.subTotalPrice,
        totalPaid = order.totalPaid,
        balance = max(0, order.totalPrice - order.totalPaid),
        status = order.status.name,
        created = order.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = order.updated.toInstant().atOffset(ZoneOffset.UTC),
        cancelled = order.cancelled?.toInstant()?.atOffset(ZoneOffset.UTC),
        closed = order.closed?.toInstant()?.atOffset(ZoneOffset.UTC)
    )

    fun search(request: SearchOrderRequest): List<OrderEntity> {
        val query = em.createQuery(sql(request))
        parameters(request, query)
        return query
            .setFirstResult(request.offset)
            .setMaxResults(request.limit)
            .resultList as List<OrderEntity>
    }

    private fun sql(request: SearchOrderRequest): String {
        val select = select()
        val where = where(request)
        return if (where.isNullOrEmpty()) {
            select
        } else {
            "$select WHERE $where ORDER BY O.created DESC"
        }
    }

    private fun select(): String =
        "SELECT O FROM OrderEntity O"

    private fun where(request: SearchOrderRequest): String {
        val criteria = mutableListOf<String>()

        if (request.customerId != null) {
            criteria.add("O.customerId = :customer_id")
        }
        if (request.businessId != null) {
            criteria.add("O.business.id = :business_id")
        }
        if (request.status.isNotEmpty()) {
            criteria.add("O.status IN :status")
        }
        if (request.createdFrom != null) {
            criteria.add("O.created >= :created_from")
        }
        if (request.createdTo != null) {
            criteria.add("O.created <= :created_to")
        }
        return criteria.joinToString(separator = " AND ")
    }

    private fun parameters(request: SearchOrderRequest, query: Query) {
        if (request.customerId != null) {
            query.setParameter("customer_id", request.customerId)
        }
        if (request.businessId != null) {
            query.setParameter("business_id", request.businessId)
        }
        if (request.status.isNotEmpty()) {
            query.setParameter("status", request.status.map { OrderStatus.valueOf(it) })
        }
        if (request.createdFrom != null) {
            query.setParameter("created_from", Date.from(request.createdFrom.toInstant()))
        }
        if (request.createdTo != null) {
            query.setParameter("created_to", Date.from(request.createdTo.toInstant()))
        }
    }

    private fun toOrderItem(item: OrderItemEntity) = OrderItem(
        productId = item.productId,
        title = item.title,
        quantity = item.quantity,
        pictureUrl = item.pictureUrl,
        unitPrice = item.unitPrice,
        totalPrice = item.totalPrice,
        totalDiscount = item.totalDiscount,
        subTotalPrice = item.subTotalPrice,
        discounts = item.discounts.map { toOrderItemDiscount(item, it) }
    )

    private fun toOrderDiscount(order: OrderEntity, discount: OrderDiscountEntity) = Discount(
        code = discount.code,
        type = discount.type.name,
        amount = discount.amount,
        rate = toRate(discount.amount, order.subTotalPrice)
    )

    private fun toOrderItemDiscount(item: OrderItemEntity, discount: OrderItemDiscountEntity) = Discount(
        code = discount.code,
        type = discount.type.name,
        amount = discount.amount,
        rate = toRate(discount.amount, item.order.subTotalPrice)
    )

    private fun toRate(value: Long, total: Long): Int =
        if (total == 0L) 0 else (100.0 * value / total).toInt()

    private fun toShortId(id: String?): String =
        id?.takeLast(4)?.uppercase() ?: ""

    private fun create(order: OrderEntity, request: CreateOrderDiscountRequest): OrderDiscountEntity =
        discountDao.save(
            OrderDiscountEntity(
                order = order,
                amount = request.amount,
                code = request.code,
                type = DiscountType.valueOf(request.type.uppercase())
            )
        )

    private fun create(order: OrderEntity, request: CreateOrderItemRequest): OrderItemEntity {
        val subTotalPrice = request.unitPrice * request.quantity
        val totalDiscount = computeTotalDiscount(request)
        val item = itemDao.save(
            OrderItemEntity(
                order = order,
                productId = request.productId,
                title = request.title,
                quantity = request.quantity,
                unitPrice = request.unitPrice,
                subTotalPrice = subTotalPrice,
                totalDiscount = totalDiscount,
                totalPrice = max(0, subTotalPrice - totalDiscount),
                pictureUrl = request.pictureUrl
            )
        )
        request.discounts.forEach {
            create(item, it)
        }
        return item
    }

    private fun create(item: OrderItemEntity, request: CreateOrderDiscountRequest): OrderItemDiscountEntity =
        itemDiscountDao.save(
            OrderItemDiscountEntity(
                orderItem = item,
                amount = request.amount,
                code = request.code,
                type = DiscountType.valueOf(request.type.uppercase())
            )
        )

    private fun computeTotalDiscount(request: CreateOrderRequest): Long =
        request.discounts.sumOf { it.amount } +
            request.items.sumOf { computeTotalDiscount(it) }

    private fun computeSubTotalPrice(request: CreateOrderRequest): Long =
        request.items.sumOf { it.quantity * it.unitPrice }

    private fun computeTotalDiscount(request: CreateOrderItemRequest): Long =
        request.discounts.sumOf { it.amount }
}
