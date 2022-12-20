package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.GetPaymentMethodDelegate
import com.wutsi.checkout.access.dto.GetPaymentMethodResponse
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.String

@RestController
public class GetPaymentMethodController(
    public val `delegate`: GetPaymentMethodDelegate,
) {
    @GetMapping("/v1/payment-methods/{token}")
    public fun invoke(@PathVariable(name = "token") token: String): GetPaymentMethodResponse =
        delegate.invoke(token)
}
