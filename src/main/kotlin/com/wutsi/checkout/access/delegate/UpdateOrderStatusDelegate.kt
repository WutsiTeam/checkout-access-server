package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
import org.springframework.stereotype.Service
import kotlin.Long

@Service
public class UpdateOrderStatusDelegate() {
    public fun invoke(id: Long, request: UpdateOrderStatusRequest) {
        TODO()
    }
}
