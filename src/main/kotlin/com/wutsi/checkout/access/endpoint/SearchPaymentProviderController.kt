package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.SearchPaymentProviderDelegate
import com.wutsi.checkout.access.dto.SearchPaymentProviderRequest
import com.wutsi.checkout.access.dto.SearchPaymentProviderResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class SearchPaymentProviderController(
    public val `delegate`: SearchPaymentProviderDelegate,
) {
    @PostMapping("/v1/payment-providers/search")
    public fun invoke(
        @Valid @RequestBody
        request: SearchPaymentProviderRequest,
    ):
        SearchPaymentProviderResponse = delegate.invoke(request)
}
