package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.delegate.UpdateOrderBalanceDelegate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
public class UpdateOrderBalanceController(
    public val `delegate`: UpdateOrderBalanceDelegate
) {
    @PostMapping("/v1/orders/{id}/balance")
    public fun invoke(@PathVariable(name = "id") id: String) {
        delegate.invoke(id)
    }
}
