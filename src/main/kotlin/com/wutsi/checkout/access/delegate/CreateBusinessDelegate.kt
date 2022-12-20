package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.CreateBusinessResponse
import com.wutsi.checkout.access.service.BusinessService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class CreateBusinessDelegate(
    private val service: BusinessService,
    private val logger: KVLogger,
) {
    @Transactional
    fun invoke(request: CreateBusinessRequest): CreateBusinessResponse {
        logger.add("request_account_id", request.accountId)
        logger.add("request_currency", request.currency)

        val business = service.create(request)
        logger.add("response_business_id", business.id)
        return CreateBusinessResponse(
            businessId = business.id ?: -1,
        )
    }
}
