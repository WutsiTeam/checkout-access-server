package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.SyncTransactionStatusDelegate
import com.wutsi.checkout.access.dto.SyncTransactionStatusResponse
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.String

@RestController
public class SyncTransactionStatusController(
    public val `delegate`: SyncTransactionStatusDelegate,
) {
    @GetMapping("/v1/transactions/{id}/status/sync")
    public fun invoke(@PathVariable(name = "id") id: String): SyncTransactionStatusResponse =
        delegate.invoke(id)
}
