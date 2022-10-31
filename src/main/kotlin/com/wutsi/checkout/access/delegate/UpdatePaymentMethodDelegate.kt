package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.dto.UpdatePaymentMethodRequest
import org.springframework.stereotype.Service

@Service
public class UpdatePaymentMethodDelegate() {
    public fun invoke(
        token: String,
        request: UpdatePaymentMethodRequest
    ) {
        TODO()
    }
}
