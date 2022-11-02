package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dto.Business
import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.enums.BusinessStatus
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.ConflictException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.payment.core.ErrorCode
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.Date

@Service
class BusinessService(
    private val dao: BusinessRepository
) {
    fun create(request: CreateBusinessRequest): BusinessEntity {
        val businesses = dao.findByAccountIdAndStatusNot(request.accountId, BusinessStatus.SUSPENDED)
        if (businesses.isNotEmpty()) {
            return businesses[0]
        }

        return dao.save(
            BusinessEntity(
                accountId = request.accountId,
                status = BusinessStatus.ACTIVE,
                currency = request.currency,
                balance = 0
            )
        )
    }

    fun updateStatus(id: Long, request: UpdateBusinessStatusRequest) {
        val business = findById(id)
        val status = BusinessStatus.valueOf(request.status.uppercase())
        if (business.status == status) {
            return
        }

        business.status = status
        when (status) {
            BusinessStatus.SUSPENDED -> business.suspended = Date()
            BusinessStatus.UNDER_REVIEW -> business.suspended = null
            BusinessStatus.ACTIVE -> business.suspended = null
            else -> throw BadRequestException(
                error = Error(
                    code = ErrorURN.STATUS_NOT_VALID.urn,
                    parameter = Parameter(
                        name = "status",
                        value = request.status,
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD
                    )
                )
            )
        }
        dao.save(business)
    }

    fun findById(id: Long): BusinessEntity =
        dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.BUSINESS_NOT_FOUND.urn,
                        parameter = Parameter(
                            name = "id",
                            value = id,
                            type = ParameterType.PARAMETER_TYPE_PATH
                        )
                    )
                )
            }

    fun toBusiness(business: BusinessEntity) = Business(
        id = business.id ?: -1,
        accountId = business.accountId,
        status = business.status.name,
        balance = business.balance,
        currency = business.currency,
        created = business.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = business.updated.toInstant().atOffset(ZoneOffset.UTC),
        suspended = business.suspended?.toInstant()?.atOffset(ZoneOffset.UTC)
    )

    fun updateBalance(business: BusinessEntity, amount: Long): BusinessEntity {
        if (business.balance + amount < 0) {
            throw ConflictException(
                error = Error(
                    code = ErrorURN.TRANSACTION_FAILED.urn,
                    downstreamCode = ErrorCode.NOT_ENOUGH_FUNDS.name
                )
            )
        }

        business.balance += amount
        return dao.save(business)
    }
}
