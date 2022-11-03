package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.delegate.SyncTransactionStatusDelegate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
public class SyncTransactionStatusController(
    public val `delegate`: SyncTransactionStatusDelegate
) {
    @GetMapping("/v1/transactions/{id}/status/sync")
    public fun invoke(@PathVariable(name = "id") id: String) {
        delegate.invoke(id)
    }
}
