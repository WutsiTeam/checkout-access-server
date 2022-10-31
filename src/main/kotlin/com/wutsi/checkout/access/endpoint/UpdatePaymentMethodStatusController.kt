package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.UpdatePaymentMethodStatusDelegate
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Any
import kotlin.String

@RestController
public class UpdatePaymentMethodStatusController(
    public val `delegate`: UpdatePaymentMethodStatusDelegate
) {
    @PostMapping("/v1/payment-methods/{token}/status")
    public fun invoke(@PathVariable(name = "token") token: String, @Valid @RequestBody request: Any) {
        delegate.invoke(token, request)
    }
}
