package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SearchPaymentProviderRequest
import com.wutsi.checkout.access.dto.SearchPaymentProviderResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.PaymentProviderService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class SearchPaymentProviderDelegate(
    private val service: PaymentProviderService,
    private val logger: KVLogger,
) {
    fun invoke(request: SearchPaymentProviderRequest): SearchPaymentProviderResponse {
        logger.add("request_country", request.country)
        logger.add("request_number", request.number)
        logger.add("request_type", request.type)

        val providers = service.search(request)
        logger.add("response_count", providers.size)

        return SearchPaymentProviderResponse(
            paymentProviders = providers.map { Mapper.toPaymentProviderSummary(it) },
        )
    }
}
