package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.SearchPaymentMethodDelegate
import com.wutsi.checkout.access.dto.SearchPaymentMethodRequest
import com.wutsi.checkout.access.dto.SearchPaymentMethodResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class SearchPaymentMethodController(
    public val `delegate`: SearchPaymentMethodDelegate,
) {
    @PostMapping("/v1/payment-methods/search")
    public fun invoke(
        @Valid @RequestBody
        request: SearchPaymentMethodRequest,
    ):
        SearchPaymentMethodResponse = delegate.invoke(request)
}
