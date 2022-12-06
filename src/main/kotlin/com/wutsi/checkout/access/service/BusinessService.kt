package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.InsuffisantFundsException
import com.wutsi.enums.BusinessStatus
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.stereotype.Service
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
                country = request.country,
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
        business.updated = Date()
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

    fun updateBalance(business: BusinessEntity, amount: Long): BusinessEntity {
        if (business.balance + amount < 0) {
            throw InsuffisantFundsException()
        }

        business.balance += amount
        business.updated = Date()
        return dao.save(business)
    }
}
