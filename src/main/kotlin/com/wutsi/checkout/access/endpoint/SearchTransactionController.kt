package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.SearchTransactionDelegate
import com.wutsi.checkout.access.dto.SearchTransactionRequest
import com.wutsi.checkout.access.dto.SearchTransactionResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class SearchTransactionController(
    public val `delegate`: SearchTransactionDelegate,
) {
    @PostMapping("/v1/transactions/search")
    public fun invoke(
        @Valid @RequestBody
        request: SearchTransactionRequest,
    ):
        SearchTransactionResponse = delegate.invoke(request)
}
