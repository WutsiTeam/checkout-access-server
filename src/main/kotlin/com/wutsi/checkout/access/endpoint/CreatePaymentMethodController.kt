package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.CreatePaymentMethodDelegate
import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.CreatePaymentMethodResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class CreatePaymentMethodController(
    public val `delegate`: CreatePaymentMethodDelegate,
) {
    @PostMapping("/v1/payment-methods")
    public fun invoke(
        @Valid @RequestBody
        request: CreatePaymentMethodRequest,
    ):
        CreatePaymentMethodResponse = delegate.invoke(request)
}
