package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.enums.PaymentMethodType
import org.springframework.stereotype.Service

@Service
class FeesCalculator {
    fun computeFees(type: PaymentMethodType, amount: Long): Long =
        0L
}
