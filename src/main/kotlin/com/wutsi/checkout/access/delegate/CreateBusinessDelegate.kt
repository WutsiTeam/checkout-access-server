package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.CreateBusinessResponse
import com.wutsi.checkout.access.service.BusinessService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateBusinessDelegate(private val service: BusinessService) {
    @Transactional
    fun invoke(request: CreateBusinessRequest): CreateBusinessResponse {
        val business = service.create(request)
        return CreateBusinessResponse(
            businessId = business.id ?: -1
        )
    }
}
