package com.wutsi.checkout.access.`delegate`

import com.wutsi.checkout.access.service.OrderService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UpdateOrderBalanceDelegate(private val service: OrderService) {
    @Transactional
    fun invoke(id: String) {
        service.updateBalance(id)
    }
}
