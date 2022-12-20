package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.GetTransactionDelegate
import com.wutsi.checkout.access.dto.GetTransactionResponse
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.String

@RestController
public class GetTransactionController(
    public val `delegate`: GetTransactionDelegate,
) {
    @GetMapping("/v1/transactions/{id}")
    public fun invoke(@PathVariable(name = "id") id: String): GetTransactionResponse =
        delegate.invoke(id)
}
