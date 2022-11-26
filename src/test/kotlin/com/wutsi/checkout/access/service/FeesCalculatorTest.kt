package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FeesCalculatorTest {
    @Autowired
    private lateinit var calculator: FeesCalculator

    @Test
    fun mobileCharge() {
        val result = calculator.compute(TransactionType.CHARGE, PaymentMethodType.MOBILE_MONEY, "CM", 1500)
        assertEquals(150, result)
    }

    @Test
    fun mobileCashout() {
        val result = calculator.compute(TransactionType.CASHOUT, PaymentMethodType.MOBILE_MONEY, "CM", 1500)
        assertEquals(0, result)
    }

    @Test
    fun bankCharge() {
        val ex = assertThrows<TransactionException> {
            calculator.compute(TransactionType.CHARGE, PaymentMethodType.BANK, "CM", 1500)
        }
        assertEquals(ErrorURN.FEES_NOT_FOUND.urn, ex.error.code)
    }

    @Test
    fun bankCashout() {
        val result = calculator.compute(TransactionType.CASHOUT, PaymentMethodType.BANK, "CM", 1500)
        assertEquals(3500, result)
    }

    @Test
    fun roundUp() {
        val result = calculator.compute(TransactionType.CHARGE, PaymentMethodType.MOBILE_MONEY, "CM", 1599)
        assertEquals(160, result)
    }
}
