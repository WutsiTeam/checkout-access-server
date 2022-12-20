package com.wutsi.checkout.access.delegate

import com.wutsi.checkout.access.dto.GetTransactionResponse
import com.wutsi.checkout.access.service.Mapper
import com.wutsi.checkout.access.service.TransactionService
import org.springframework.stereotype.Service

@Service
public class GetTransactionDelegate(private val service: TransactionService) {
    public fun invoke(id: String): GetTransactionResponse {
        val tx = service.findById(id)
        return GetTransactionResponse(
            transaction = Mapper.toTransaction(tx),
        )
    }
}
