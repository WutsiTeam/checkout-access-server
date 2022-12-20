package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import com.wutsi.checkout.access.service.OrderService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdateOrderStatusDelegate(
    private val service: OrderService,
    private val logger: KVLogger,
) {
    @Transactional
    fun invoke(id: String, request: UpdateOrderStatusRequest) {
        logger.add("request_status", request.status)
        service.updateStatus(id, request)
    }
}
