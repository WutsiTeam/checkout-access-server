package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.PaymentProviderRepository
import com.wutsi.checkout.access.dto.PaymentProviderSummary
import com.wutsi.checkout.access.entity.PaymentProviderEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class PaymentProviderService(
    private val dao: PaymentProviderRepository
) {
    fun findById(id: Long): PaymentProviderEntity =
        dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.PAYMENT_PROVIDER_NOT_FOUND.urn,
                        parameter = Parameter(
                            value = id
                        )
                    )
                )
            }

    fun toPaymentProviderSummary(provider: PaymentProviderEntity) = PaymentProviderSummary(
        id = provider.id ?: -1,
        code = provider.code,
        name = provider.name,
        logoUrl = provider.logoUrl,
        type = provider.type.name
    )
}
