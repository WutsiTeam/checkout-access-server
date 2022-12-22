package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SearchOrderRequest
import com.wutsi.checkout.access.dto.SearchOrderResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.OrderService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class SearchOrderDelegate(
    private val service: OrderService,
    private val logger: KVLogger,
) {
    fun invoke(request: SearchOrderRequest): SearchOrderResponse {
        logger.add("request_limit", request.limit)
        logger.add("request_offset", request.offset)
        logger.add("request_status", request.status)
        logger.add("request_created_from", request.createdFrom)
        logger.add("request_created_to", request.createdTo)
        logger.add("request_expires_to", request.expiresTo)
        logger.add("request_business_id", request.businessId)
        logger.add("request_customer_account_id", request.customerAccountId)

        val orders = service.search(request)
        logger.add("response_count", orders.size)
        return SearchOrderResponse(
            orders = orders.map { Mapper.toOrderSummary(it) },
        )
    }
}
