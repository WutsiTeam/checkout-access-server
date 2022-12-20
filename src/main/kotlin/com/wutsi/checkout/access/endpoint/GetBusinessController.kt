package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.GetBusinessDelegate
import com.wutsi.checkout.access.dto.GetBusinessResponse
import org.springframework.web.bind.`annotation`.GetMapping
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RestController
import kotlin.Long

@RestController
public class GetBusinessController(
    public val `delegate`: GetBusinessDelegate,
) {
    @GetMapping("/v1/businesses/{id}")
    public fun invoke(@PathVariable(name = "id") id: Long): GetBusinessResponse = delegate.invoke(id)
}
