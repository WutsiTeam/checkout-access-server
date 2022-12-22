package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.CreateOrderRequest
import com.wutsi.checkout.access.dto.CreateOrderResponse
import com.wutsi.checkout.access.service.BusinessService
import com.wutsi.checkout.access.service.OrderService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateOrderDelegate(
    private val service: OrderService,
    private val businessService: BusinessService,
    private val logger: KVLogger,
) {
    @Transactional
    fun invoke(request: CreateOrderRequest): CreateOrderResponse {
        logger.add("request_customer_account_id", request.customerAccountId)
        logger.add("request_customer_email", request.customerEmail)
        logger.add("request_customer_name", request.customerName)
        logger.add("request_business_id", request.businessId)
        logger.add("request_channel_type", request.channelType)
        logger.add("request_currency", request.currency)
        logger.add("request_device_type", request.deviceType)

        val business = businessService.findById(request.businessId)
        val order = service.create(business, request)
        logger.add("response_order_id", order.id)

        return CreateOrderResponse(
            orderId = order.id ?: "",
            orderStatus = order.status.name,
        )
    }
}
