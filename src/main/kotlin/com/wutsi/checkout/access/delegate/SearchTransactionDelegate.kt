package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SearchTransactionRequest
import com.wutsi.checkout.access.dto.SearchTransactionResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.TransactionService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class SearchTransactionDelegate(
    private val service: TransactionService,
    private val logger: KVLogger,
) {
    fun invoke(request: SearchTransactionRequest): SearchTransactionResponse {
        logger.add("request_business_id", request.businessId)
        logger.add("request_customer_account_id", request.customerAccountId)
        logger.add("request_status", request.status)
        logger.add("request_type", request.type)
        logger.add("request_order_id", request.orderId)
        logger.add("request_limit", request.limit)
        logger.add("request_offset", request.offset)

        val txs = service.search(request)
        logger.add("response_count", txs.size)

        return SearchTransactionResponse(
            transactions = txs.map { Mapper.toTransactionSummary(it) },
        )
    }
}
