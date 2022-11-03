package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.service.TransactionService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SyncTransactionStatusDelegate(private val service: TransactionService) {
    @Transactional
    fun invoke(id: String) {
        service.syncStatus(id)
    }
}
