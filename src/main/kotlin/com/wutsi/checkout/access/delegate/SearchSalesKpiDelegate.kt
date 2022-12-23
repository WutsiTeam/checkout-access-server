package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SalesKpiSummary
import com.wutsi.checkout.access.dto.SearchSalesKpiRequest
import com.wutsi.checkout.access.dto.SearchSalesKpiResponse
import com.wutsi.checkout.access.service.SalesKpiService
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
public class SearchSalesKpiDelegate(
    private val logger: KVLogger,
    private val service: SalesKpiService,
) {
    public fun invoke(request: SearchSalesKpiRequest): SearchSalesKpiResponse {
        logger.add("request_from_date", request.fromDate)
        logger.add("request_to_date", request.toDate)
        logger.add("request_business_id", request.businessId)
        logger.add("request_product_id", request.productId)
        logger.add("request_aggregate", request.aggregate)

        val kpis = service.search(request)
        logger.add("response_count", kpis.size)

        return SearchSalesKpiResponse(
            kpis = kpis.map {
                SalesKpiSummary(
                    date = it.date.toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
                    totalOrders = it.totalOrders,
                    totalUnits = it.totalUnits,
                    totalValue = it.totalValue,
                )
            },
        )
    }
}
