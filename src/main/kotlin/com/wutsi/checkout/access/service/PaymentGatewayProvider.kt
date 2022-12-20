package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.InternalErrorException
import com.wutsi.platform.payment.Gateway
import com.wutsi.platform.payment.provider.flutterwave.FWGateway
import org.springframework.stereotype.Service

@Service
class PaymentGatewayProvider(
    private val flutterwave: FWGateway,
) {
    fun get(type: PaymentMethodType): Gateway = when (type) {
        PaymentMethodType.MOBILE_MONEY -> flutterwave
        PaymentMethodType.BANK -> flutterwave
        else -> throw InternalErrorException(
            error = Error(
                code = ErrorURN.PAYMENT_GATEWAY_NOT_SUPPORTED.urn,
                data = mapOf(
                    "payment-method-type" to type,
                ),
            ),
        )
    }
}
