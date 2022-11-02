package com.wutsi.checkout.access.`delegate`

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
    private val logger: KVLogger
) {
    @Transactional
    fun invoke(request: CreateOrderRequest): CreateOrderResponse {
        val business = businessService.findById(request.businessId)
        val order = service.create(business, request)
        logger.add("order_id", order.id)

        return CreateOrderResponse(
            orderId = order.id ?: ""
        )
    }
}
