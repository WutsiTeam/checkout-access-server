package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.UpdateBusinessStatusDelegate
import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid
import kotlin.Long

@RestController
public class UpdateBusinessStatusController(
    public val `delegate`: UpdateBusinessStatusDelegate,
) {
    @PostMapping("/v1/businesses/{id}/status")
    public fun invoke(
        @PathVariable(name = "id") id: Long,
        @Valid @RequestBody
        request: UpdateBusinessStatusRequest,
    ) {
        delegate.invoke(id, request)
    }
}
