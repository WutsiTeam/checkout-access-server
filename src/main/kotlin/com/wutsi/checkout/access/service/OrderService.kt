package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.OrderDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemRepository
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dto.CreateOrderDiscountRequest
import com.wutsi.checkout.access.dto.CreateOrderItemRequest
import com.wutsi.checkout.access.dto.CreateOrderRequest
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.checkout.access.entity.OrderDiscountEntity
import com.wutsi.checkout.access.entity.OrderEntity
import com.wutsi.checkout.access.entity.OrderItemDiscountEntity
import com.wutsi.checkout.access.entity.OrderItemEntity
import com.wutsi.checkout.access.enums.ChannelType
import com.wutsi.checkout.access.enums.DeviceType
import com.wutsi.checkout.access.enums.DiscountType
import com.wutsi.checkout.access.enums.OfferType
import com.wutsi.checkout.access.enums.OrderStatus
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.stereotype.Service
import java.lang.Long.max
import java.util.Date
import java.util.UUID

@Service
class OrderService(
    private val dao: OrderRepository,
    private val itemDao: OrderItemRepository,
    private val discountDao: OrderDiscountRepository,
    private val itemDiscountDao: OrderItemDiscountRepository
) {
    fun create(request: CreateOrderRequest): OrderEntity {
        // Order
        val subTotalPrice = computeSubTotalPrice(request)
        val totalDiscount = computeTotalDiscount(request)
        val order = dao.save(
            OrderEntity(
                id = UUID.randomUUID().toString(),
                storeId = request.storeId,
                customerId = request.customerId,
                customerEmail = request.customerEmail,
                customerName = request.customerName,
                status = OrderStatus.OPENED,
                currency = request.currency,
                deviceId = request.deviceId,
                deviceIp = request.deviceIp,
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

        // Never update an order closed
        if (order.status == OrderStatus.CANCELLED || order.status == OrderStatus.CLOSED) {
            throw ConflictException(
                error = Error(
                    code = ErrorURN.ORDER_CLOSED.urn,
                    parameter = Parameter(
                        name = "status",
                        value = request.status,
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD
                    )
                )
            )
        }

        // Change status
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
        val subTotalPrice = request.price * request.quantity
        val totalDiscount = computeTotalDiscount(request)
        val item = itemDao.save(
            OrderItemEntity(
                order = order,
                offerId = request.offerId,
                offerType = OfferType.valueOf(request.offerType),
                title = request.title,
                quantity = request.quantity,
                unitPrice = request.price,
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
        request.items.sumOf { it.quantity * it.price }

    private fun computeTotalDiscount(request: CreateOrderItemRequest): Long =
        request.discounts.sumOf { it.amount }
}
