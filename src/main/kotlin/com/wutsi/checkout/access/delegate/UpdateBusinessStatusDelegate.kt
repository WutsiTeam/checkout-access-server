package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.checkout.access.service.BusinessService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdateBusinessStatusDelegate(
    private val service: BusinessService,
    private val logger: KVLogger,
) {
    @Transactional
    fun invoke(id: Long, request: UpdateBusinessStatusRequest) {
        logger.add("request_status", request.status)
        service.updateStatus(id, request)
    }
}
