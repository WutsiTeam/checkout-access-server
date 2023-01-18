package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.TransactionRepository
import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.checkout.access.entity.BusinessEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.checkout.access.error.InsuffisantFundsException
import com.wutsi.enums.BusinessStatus
import com.wutsi.enums.TransactionType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.core.Status
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Date
import javax.sql.DataSource
import kotlin.math.max

@Service
class BusinessService(
    private val dao: BusinessRepository,
    private val txDao: TransactionRepository,
    private val ds: DataSource,
    private val logger: KVLogger,
    @Value("\${wutsi.application.cashout.delay-days}") private val cashoutDelay: Long,
) {
    fun create(request: CreateBusinessRequest): BusinessEntity {
        val businesses = dao.findByAccountIdAndStatusNotIn(
            request.accountId,
            listOf(BusinessStatus.UNKNOWN, BusinessStatus.INACTIVE),
        )
        if (businesses.isNotEmpty()) {
            return businesses[0]
        }

        return dao.save(
            BusinessEntity(
                accountId = request.accountId,
                status = BusinessStatus.ACTIVE,
                currency = request.currency,
                country = request.country,
                balance = 0,
            ),
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
            BusinessStatus.INACTIVE -> business.deactivated = Date()
            BusinessStatus.UNDER_REVIEW -> business.deactivated = null
            BusinessStatus.ACTIVE -> business.deactivated = null
            else -> throw BadRequestException(
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
                            type = ParameterType.PARAMETER_TYPE_PATH,
                        ),
                    ),
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

    fun computeCashoutBalance(business: BusinessEntity): Long {
        if (business.balance == 0L) {
            return 0
        }
        val threshold = Date.from(OffsetDateTime.now().minusDays(cashoutDelay).toInstant())
        val txs = txDao.findByBusinessAndTypeAndStatusAndCreatedGreaterThanEqual(
            business,
            TransactionType.CHARGE,
            Status.SUCCESSFUL,
            threshold,
        )
        val totalCharges = txs.sumOf { it.net }

        logger.add("balance", business.balance)
        logger.add("total_changes_${cashoutDelay}d", totalCharges)
        return max(0L, business.balance - totalCharges)
    }

    fun updateSalesKpi(date: LocalDate): Long {
        val sql =
            """
                UPDATE T_BUSINESS B,
                    (
                        SELECT K.business_fk, SUM(K.total_orders) as total_orders, SUM(K.total_value) as total_value, SUM(K.total_views) as total_views
                            FROM T_KPI_SALES K
                            WHERE K.business_fk IN (SELECT O.business_fk FROM T_ORDER O WHERE DATE(O.created) = '$date')
                            GROUP by K.business_fk
                    ) TMP
                    SET
                        B.total_orders=TMP.total_orders,
                        B.total_sales=TMP.total_value,
                        B.total_views=TMP.total_views
                    WHERE
                        B.id=TMP.business_fk

            """.trimIndent()
        val cnn = ds.connection
        cnn.use {
            val stmt = cnn.prepareStatement(sql)
            stmt.use {
                return stmt.executeUpdate().toLong()
            }
        }
    }
}
