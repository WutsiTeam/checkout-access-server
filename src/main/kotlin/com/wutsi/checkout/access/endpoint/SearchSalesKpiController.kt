package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.SearchSalesKpiDelegate
import com.wutsi.checkout.access.dto.SearchSalesKpiRequest
import com.wutsi.checkout.access.dto.SearchSalesKpiResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class SearchSalesKpiController(
    public val `delegate`: SearchSalesKpiDelegate,
) {
    @PostMapping("/v1/kpis/sales/search")
    public fun invoke(
        @Valid @RequestBody
        request: SearchSalesKpiRequest,
    ): SearchSalesKpiResponse =
        delegate.invoke(request)
}
