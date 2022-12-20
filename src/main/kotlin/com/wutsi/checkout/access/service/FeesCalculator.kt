package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.TransactionException
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.Error
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.math.RoundingMode
import javax.annotation.PostConstruct

data class TransactionFee(
    var country: String = "",
    var transactionType: String = "",
    var paymentMethodType: String = "",
    var percent: Int = 0,
    var amount: Long = 0,
)

@Service
@ConfigurationProperties(prefix = "wutsi.application.services.fees-calculator")
class FeesCalculator {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FeesCalculator::class.java)
    }

    var transactionFees: MutableList<TransactionFee> = mutableListOf()

    @PostConstruct
    fun init() {
        transactionFees.forEach {
            LOGGER.info("${it.country} ${it.transactionType} ${it.paymentMethodType}= ${it.percent}% + ${it.amount}")
        }
    }

    fun compute(
        transactionType: TransactionType,
        paymentMethodType: PaymentMethodType,
        country: String,
        amount: Long,
    ): Long {
        val fee: TransactionFee = transactionFees.find {
            it.transactionType == transactionType.name &&
                it.paymentMethodType == paymentMethodType.name &&
                it.country == country
        }
            ?: throw TransactionException(
                error = Error(
                    code = ErrorURN.FEES_NOT_FOUND.urn,
                ),
            )

        val value = amount * fee.percent.toDouble() / 100.0 + fee.amount
        return value.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
