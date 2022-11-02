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
import com.wutsi.platform.core.error.exception.NotFoundException
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
                status = BusinessStatus.ACTIVE
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
            else -> BadRequestException(
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
        created = business.created.toInstant().atOffset(ZoneOffset.UTC),
        updated = business.updated.toInstant().atOffset(ZoneOffset.UTC),
        suspended = business.suspended?.toInstant()?.atOffset(ZoneOffset.UTC)
    )
}
