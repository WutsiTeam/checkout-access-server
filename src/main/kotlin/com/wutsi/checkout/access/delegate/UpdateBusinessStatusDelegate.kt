package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.checkout.access.service.BusinessService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdateBusinessStatusDelegate(private val service: BusinessService) {
    @Transactional
    fun invoke(id: Long, request: UpdateBusinessStatusRequest) {
        service.updateStatus(id, request)
    }
}
