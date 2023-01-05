package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.OrderDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemRepository
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateOrderDiscountRequest
import com.wutsi.checkout.access.dto.CreateOrderItemRequest
import com.wutsi.checkout.access.dto.CreateOrderRequest
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
import com.wutsi.enums.ProductType
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
    private val tracingContext: TracingContext,
) {
    fun create(business: BusinessEntity, request: CreateOrderRequest): OrderEntity {
        // Order
        val subTotalPrice = computeSubTotalPrice(request)
        val totalDiscount = computeTotalDiscount(request)
        val totalPrice = max(0L, subTotalPrice - totalDiscount)
        val order = dao.save(
            OrderEntity(
                id = UUID.randomUUID().toString(),
                business = business,
                customerAccountId = request.customerAccountId,
                customerEmail = request.customerEmail.lowercase(),
                customerName = request.customerName,
                status = if (totalPrice <= 0) OrderStatus.OPENED else OrderStatus.PENDING,
                currency = request.currency,
                deviceId = tracingContext.deviceId(),
                deviceType = request.deviceType?.let { DeviceType.valueOf(it.uppercase()) },
                subTotalPrice = subTotalPrice,
                totalDiscount = totalDiscount,
                totalPrice = totalPrice,
                channelType = request.channelType?.let { ChannelType.valueOf(it.uppercase()) },
                notes = request.notes,
                expires = request.expires?.let {
                    Date(it.toInstant().toEpochMilli())
                } ?: Date(System.currentTimeMillis() + 30 * 60 * 1000),
                itemCount = request.items.size,
                productPictureUrl1 = request.items[0].pictureUrl,
                productPictureUrl2 = if (request.items.size > 1) request.items[1].pictureUrl else null,
                productPictureUrl3 = if (request.items.size > 2) request.items[2].pictureUrl else null,
            ),
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
        order.updated = Date()
        when (status) {
            OrderStatus.COMPLETED -> order.closed = Date()
            OrderStatus.EXPIRED -> order.expired = Date()
            OrderStatus.IN_PROGRESS -> {}
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
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD,
                    ),
                ),
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
                            type = ParameterType.PARAMETER_TYPE_PATH,
                        ),
                    ),
                )
            }

    fun updateBalance(order: OrderEntity) {
        order.totalPaid = transactionDao.findByOrderAndStatus(order, Status.SUCCESSFUL)
            .filter { it.type == TransactionType.CHARGE }
            .sumOf { it.amount }
        order.updated = Date()
        dao.save(order)
    }

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
        val from = from(request)
        val where = where(request)
        return if (where.isNullOrEmpty()) {
            select
        } else {
            "$select $from WHERE $where ORDER BY O.created DESC"
        }
    }

    private fun select(): String =
        "SELECT O"

    private fun from(request: SearchOrderRequest): String =
        if (request.productId == null) {
            "FROM OrderEntity O"
        } else {
            "FROM OrderEntity O JOIN O.items I"
        }

    private fun where(request: SearchOrderRequest): String {
        val criteria = mutableListOf<String>()

        if (request.customerAccountId != null) {
            criteria.add("O.customerAccountId = :customer_account_id")
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
        if (request.expiresTo != null) {
            criteria.add("O.expires <= :expires_to")
        }
        if (request.productId != null) {
            criteria.add("I.productId = :product_id")
        }
        return criteria.joinToString(separator = " AND ")
    }

    private fun parameters(request: SearchOrderRequest, query: Query) {
        if (request.customerAccountId != null) {
            query.setParameter("customer_account_id", request.customerAccountId)
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
        if (request.expiresTo != null) {
            query.setParameter("expires_to", Date.from(request.expiresTo.toInstant()))
        }
        if (request.productId != null) {
            query.setParameter("product_id", request.productId)
        }
    }

    private fun create(order: OrderEntity, request: CreateOrderDiscountRequest): OrderDiscountEntity =
        discountDao.save(
            OrderDiscountEntity(
                order = order,
                amount = request.amount,
                name = request.name,
                type = DiscountType.valueOf(request.type.uppercase()),
                discountId = request.discountId,
            ),
        )

    private fun create(order: OrderEntity, request: CreateOrderItemRequest): OrderItemEntity {
        val subTotalPrice = request.unitPrice * request.quantity
        val totalDiscount = computeTotalDiscount(request)
        val item = itemDao.save(
            OrderItemEntity(
                order = order,
                productId = request.productId,
                productType = ProductType.valueOf(request.productType),
                title = request.title,
                quantity = request.quantity,
                unitPrice = request.unitPrice,
                subTotalPrice = subTotalPrice,
                totalDiscount = totalDiscount,
                totalPrice = max(0, subTotalPrice - totalDiscount),
                pictureUrl = request.pictureUrl,
            ),
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
                name = request.name,
                type = DiscountType.valueOf(request.type.uppercase()),
                discountId = request.discountId,
            ),
        )

    private fun computeTotalDiscount(request: CreateOrderRequest): Long =
        request.discounts.sumOf { it.amount } +
            request.items.sumOf { computeTotalDiscount(it) }

    private fun computeSubTotalPrice(request: CreateOrderRequest): Long =
        request.items.sumOf { it.quantity * it.unitPrice }

    private fun computeTotalDiscount(request: CreateOrderItemRequest): Long =
        request.discounts.sumOf { it.amount }
}
