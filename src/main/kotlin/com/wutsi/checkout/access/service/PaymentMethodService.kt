package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.PaymentMethodRepository
import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.PaymentMethod
import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.enums.PaymentMethodStatus
import com.wutsi.checkout.access.enums.PaymentMethodType
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.UUID

@Service
class PaymentMethodService(private val dao: PaymentMethodRepository) {
    fun create(request: CreatePaymentMethodRequest): PaymentMethodEntity {
        val type = PaymentMethodType.valueOf(request.type.uppercase())
        val number = request.number.uppercase()
        val paymentMethods = dao.findByTypeAndNumberAndStatus(type, number, PaymentMethodStatus.ACTIVE)
        if (paymentMethods.isNotEmpty()) {
            return paymentMethods.find { it.accountId == request.accountId }
                ?: throw ConflictException(
                    error = Error(
                        code = ErrorURN.PAYMENT_METHOD_ALREADY_ASSIGNED.urn,
                        parameter = Parameter(
                            name = "number",
                            value = mask(request.number),
                            type = ParameterType.PARAMETER_TYPE_PAYLOAD
                        )
                    )
                )
        }

        return dao.save(
            PaymentMethodEntity(
                accountId = request.accountId,
                type = type,
                number = number,
                status = PaymentMethodStatus.ACTIVE,
                country = request.country,
                ownerName = request.ownerName,
                token = hash()
            )
        )
    }

    fun findByToken(token: String): PaymentMethodEntity =
        dao.findByToken(token)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.PAYMENT_METHOD_NOT_FOUND.urn,
                        parameter = Parameter(
                            name = "token",
                            value = token,
                            type = ParameterType.PARAMETER_TYPE_PATH
                        )
                    )
                )
            }

    fun mask(number: String): String =
        "xxxx" + number.takeLast(4)

    fun toPaymentMethod(payment: PaymentMethodEntity) = PaymentMethod(
        accountId = payment.accountId,
        token = payment.token,
        type = payment.type.name,
        status = payment.status.name,
        number = mask(payment.number),
        ownerName = payment.ownerName,
        country = payment.country,
        created = payment.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = payment.updated.toInstant().atOffset(ZoneOffset.UTC),
        deactivated = payment.deactivated?.toInstant()?.atOffset(ZoneOffset.UTC)
    )

    private fun hash(): String =
        UUID.randomUUID().toString().lowercase()
}
