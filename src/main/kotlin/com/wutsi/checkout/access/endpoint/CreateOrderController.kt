package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.`delegate`.CreateOrderDelegate
import com.wutsi.checkout.access.dto.CreateOrderRequest
import com.wutsi.checkout.access.dto.CreateOrderResponse
import org.springframework.web.bind.`annotation`.PostMapping
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RestController
import javax.validation.Valid

@RestController
public class CreateOrderController(
    public val `delegate`: CreateOrderDelegate,
) {
    @PostMapping("/v1/orders")
    public fun invoke(
        @Valid @RequestBody
        request: CreateOrderRequest,
    ): CreateOrderResponse =
        delegate.invoke(request)
}
