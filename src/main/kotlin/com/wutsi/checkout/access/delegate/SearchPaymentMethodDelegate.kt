package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SearchPaymentMethodRequest
import com.wutsi.checkout.access.dto.SearchPaymentMethodResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.PaymentMethodService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class SearchPaymentMethodDelegate(
    private val service: PaymentMethodService,
    private val logger: KVLogger,
) {
    fun invoke(request: SearchPaymentMethodRequest): SearchPaymentMethodResponse {
        logger.add("request_account_id", request.accountId)
        logger.add("request_status", request.status)
        logger.add("request_limit", request.limit)
        logger.add("request_offset", request.offset)

        val payments = service.search(request)
        logger.add("response_count", payments.size)
        return SearchPaymentMethodResponse(
            paymentMethods = payments.map { Mapper.toPaymentMethodSummary(it) },
        )
    }
}
