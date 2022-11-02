package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.entity.TransactionEntity
import org.springframework.stereotype.Service

@Service
class FeesCalculator {
    fun computeFees(tx: TransactionEntity): Long =
        0L
}
