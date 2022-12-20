package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.PaymentMethodRepository
import com.wutsi.checkout.access.dto.CreatePaymentMethodRequest
import com.wutsi.checkout.access.dto.SearchPaymentMethodRequest
import com.wutsi.checkout.access.dto.UpdatePaymentMethodStatusRequest
import com.wutsi.checkout.access.entity.PaymentMethodEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class PaymentMethodService(
    private val dao: PaymentMethodRepository,
    private val paymentProviderService: PaymentProviderService,
) {
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
                            value = request.number,
                            type = ParameterType.PARAMETER_TYPE_PAYLOAD,
                        ),
                    ),
                )
        }

        return dao.save(
            PaymentMethodEntity(
                accountId = request.accountId,
                provider = paymentProviderService.findById(request.providerId),
                type = type,
                number = number,
                status = PaymentMethodStatus.ACTIVE,
                country = request.country,
                ownerName = request.ownerName,
                token = hash(),
            ),
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
                            type = ParameterType.PARAMETER_TYPE_PATH,
                        ),
                    ),
                )
            }

    fun mask(number: String): String =
        "....${number.takeLast(4)}"

    fun search(request: SearchPaymentMethodRequest): List<PaymentMethodEntity> {
        val pagination = PageRequest.of(request.offset / request.limit, request.limit)
        return if (request.status == null) {
            dao.findByAccountId(request.accountId, pagination)
        } else {
            val status = PaymentMethodStatus.valueOf(request.status.uppercase())
            dao.findByAccountIdAndStatus(request.accountId, status, pagination)
        }
    }

    fun updateStatus(token: String, request: UpdatePaymentMethodStatusRequest) {
        val paymentMethod = findByToken(token)
        val status = PaymentMethodStatus.valueOf(request.status.uppercase())
        if (status == paymentMethod.status) {
            return
        }

        paymentMethod.status = status
        when (status) {
            PaymentMethodStatus.ACTIVE -> paymentMethod.deactivated = null
            PaymentMethodStatus.INACTIVE -> paymentMethod.deactivated = Date()
            else -> BadRequestException(
                error = Error(
                    code = ErrorURN.STATUS_NOT_VALID.urn,
                    parameter = Parameter(
                        name = "status",
                        value = request.status,
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD,
                    ),
                ),
            )
        }
        dao.save(paymentMethod)
    }

    private fun hash(): String =
        UUID.randomUUID().toString().lowercase()
}
