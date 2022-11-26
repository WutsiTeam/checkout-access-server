package com.wutsi.checkout.access.service

import com.nhaarman.mockitokotlin2.mock
import com.wutsi.enums.PaymentMethodType
import com.wutsi.platform.core.error.exception.InternalErrorException
import com.wutsi.platform.payment.provider.flutterwave.FWGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class PaymentGatewayProviderTest {
    private lateinit var flutterwave: FWGateway
    private lateinit var provider: PaymentGatewayProvider

    @BeforeEach
    fun setUp() {
        flutterwave = mock()
        provider = PaymentGatewayProvider(flutterwave)
    }

    @Test
    fun get() {
        assertEquals(flutterwave, provider.get(PaymentMethodType.MOBILE_MONEY))
        assertEquals(flutterwave, provider.get(PaymentMethodType.BANK))
    }

    @Test
    fun error() {
        assertThrows<InternalErrorException> {
            provider.get(PaymentMethodType.UNKNOWN)
        }
    }
}
