package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.GetBusinessResponse
import com.wutsi.checkout.access.service.BusinessService
import org.springframework.stereotype.Service

@Service
class GetBusinessDelegate(private val service: BusinessService) {
    fun invoke(id: Long): GetBusinessResponse {
        val business = service.findById(id)
        return GetBusinessResponse(
            business = service.toBusiness(business)
        )
    }
}
