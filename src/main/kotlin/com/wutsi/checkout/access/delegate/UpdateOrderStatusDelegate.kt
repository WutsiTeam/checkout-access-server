package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.checkout.access.service.OrderService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
public class UpdateOrderStatusDelegate(private val service: OrderService) {
    @Transactional
    public fun invoke(id: String, request: UpdateOrderStatusRequest) {
        service.updateStatus(id, request)
    }
}
