package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.SearchOrderRequest
import com.wutsi.checkout.access.dto.SearchOrderResponse
import com.wutsi.checkout.access.service.OrderService
import org.springframework.stereotype.Service

@Service
class SearchOrderDelegate(private val service: OrderService) {
    fun invoke(request: SearchOrderRequest): SearchOrderResponse {
        val orders = service.search(request)
        return SearchOrderResponse(
            orders = orders.map { service.toOrderSummary(it) }
        )
    }
}
