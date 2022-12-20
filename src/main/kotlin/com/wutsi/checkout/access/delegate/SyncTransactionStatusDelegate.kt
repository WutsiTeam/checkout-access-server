package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.SyncTransactionStatusResponse
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.checkout.access.service.TransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SyncTransactionStatusDelegate(private val service: TransactionService) {
    @Transactional(noRollbackFor = [TransactionException::class])
    fun invoke(id: String): SyncTransactionStatusResponse {
        val status = service.syncStatus(id)
        return SyncTransactionStatusResponse(
            transactionId = id,
            status = status.name,
        )
    }
}
