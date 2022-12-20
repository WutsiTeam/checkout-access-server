package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.GetOrderResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.OrderService
import org.springframework.stereotype.Service

@Service
class GetOrderDelegate(val service: OrderService) {
    fun invoke(id: String): GetOrderResponse {
        val order = service.findById(id)
        return GetOrderResponse(
            order = Mapper.toOrder(order),
        )
    }
}
