package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.CreateBusinessDelegate
import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.CreateBusinessResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class CreateBusinessController(
    public val `delegate`: CreateBusinessDelegate,
) {
    @PostMapping("/v1/businesses")
    public fun invoke(
        @Valid @RequestBody
        request: CreateBusinessRequest,
    ): CreateBusinessResponse =
        delegate.invoke(request)
}
