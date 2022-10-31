package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.UpdatePaymentMethodDelegate
import com.wutsi.checkout.access.dto.UpdatePaymentMethodRequest
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.String

@RestController
public class UpdatePaymentMethodController(
    public val `delegate`: UpdatePaymentMethodDelegate
) {
    @PostMapping("/v1/payment-methods/{token}")
    public fun invoke(
        @PathVariable(name = "token") token: String,
        @Valid @RequestBody
        request: UpdatePaymentMethodRequest
    ) {
        delegate.invoke(token, request)
    }
}
