package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.UpdateOrderStatusDelegate
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.String

@RestController
public class UpdateOrderStatusController(
    public val `delegate`: UpdateOrderStatusDelegate,
) {
    @PostMapping("/v1/orders/{id}/status")
    public fun invoke(
        @PathVariable(name = "id") id: String,
        @Valid @RequestBody
        request: UpdateOrderStatusRequest,
    ) {
        delegate.invoke(id, request)
    }
}
