package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.SearchOrderRequest
import com.wutsi.checkout.access.dto.SearchOrderResponse
import org.springframework.stereotype.Service

@Service
public class SearchOrderDelegate() {
    public fun invoke(request: SearchOrderRequest): SearchOrderResponse {
        TODO()
    }
}
