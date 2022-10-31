package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.DeletePaymentMethodDelegate
import org.springframework.web.bind.`annotation`.DeleteMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.String

@RestController
public class DeletePaymentMethodController(
    public val `delegate`: DeletePaymentMethodDelegate
) {
    @DeleteMapping("/v1/payment-methods/{token}")
    public fun invoke(@PathVariable(name = "token") token: String) {
        delegate.invoke(token)
    }
}
