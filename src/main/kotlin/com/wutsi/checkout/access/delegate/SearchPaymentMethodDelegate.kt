package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.SearchPaymentMethodRequest
import com.wutsi.checkout.access.dto.SearchPaymentMethodResponse
import org.springframework.stereotype.Service

@Service
public class SearchPaymentMethodDelegate() {
    public fun invoke(request: SearchPaymentMethodRequest): SearchPaymentMethodResponse {
        TODO()
    }
}
