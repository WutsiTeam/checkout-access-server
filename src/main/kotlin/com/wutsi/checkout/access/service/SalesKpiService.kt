package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dto.SearchSalesKpiRequest
import com.wutsi.checkout.access.entity.SalesKpiEntity
import com.wutsi.enums.OrderStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.sql.DataSource
import javax.transaction.Transactional

@Service
class SalesKpiService(
    private val em: EntityManager,
    private val ds: DataSource,
) {
    @Transactional
    fun compute(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI_SALES(date, business_fk, customer_fk, product_id, total_orders, total_units, total_value)
                    SELECT date, business_fk, customer_fk, product_id, total_orders, total_units, total_value FROM
                    (
                        SELECT
                                DATE(O.created) AS date,
                                O.business_fk, C.id as customer_fk,
                                I.product_id,
                                COUNT(I.product_id) AS total_orders,
                                SUM(I.quantity) AS total_units,
                                SUM(I.total_price) AS  total_value
                            FROM T_ORDER O
                                JOIN T_ORDER_ITEM I ON I.order_fk=O.id
                                JOIN T_CUSTOMER C ON O.customer_email=C.email
                            WHERE
                                DATE(O.created)='$date'
                                AND O.status NOT IN (
                                    ${OrderStatus.UNKNOWN.ordinal},
                                    ${OrderStatus.PENDING.ordinal},
                                    ${OrderStatus.EXPIRED.ordinal}
                                )
                            GROUP BY DATE(O.created), O.business_fk, C.id, I.product_id
                    ) TMP
                ON DUPLICATE KEY UPDATE total_orders=TMP.total_orders, total_units=TMP.total_units, total_value=TMP.total_value
            """.trimIndent(),
        )

    fun search(request: SearchSalesKpiRequest): List<SalesKpiEntity> {
        val sql = sql(request)
        val query = em.createQuery(sql)
        parameters(request, query)
        val kpis = query.resultList as List<SalesKpiEntity>
        if (request.aggregate && kpis.isNotEmpty()) {
            return listOf(
                SalesKpiEntity(
                    date = kpis[0].date,
                    totalValue = kpis.sumOf { it.totalValue },
                    totalUnits = kpis.sumOf { it.totalUnits },
                    totalOrders = kpis.sumOf { it.totalOrders },
                ),
            )
        }

        return kpis
    }

    private fun sql(request: SearchSalesKpiRequest): String {
        val select = select(request)
        val where = where(request)
        val orderBy = orderBy(request)
        return "$select WHERE $where $orderBy"
    }

    private fun select(request: SearchSalesKpiRequest): String =
        "SELECT a FROM SalesKpiEntity a"

    private fun where(request: SearchSalesKpiRequest): String {
        val criteria = mutableListOf<String>()

        if (request.businessId != null) {
            criteria.add("a.business.id=:business_id")
        }
        if (request.productId != null) {
            criteria.add("a.productId=:product_id")
        }
        if (request.fromDate != null) {
            criteria.add("a.date >= :from_date")
        }
        if (request.toDate != null) {
            criteria.add("a.date <= :to_date")
        }
        return criteria.joinToString(separator = " AND ")
    }

    private fun parameters(request: SearchSalesKpiRequest, query: Query) {
        if (request.businessId != null) {
            query.setParameter("business_id", request.businessId)
        }
        if (request.productId != null) {
            query.setParameter("product_id", request.productId)
        }
        if (request.fromDate != null) {
            query.setParameter("from_date", Date.from(request.fromDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
        }
        if (request.toDate != null) {
            query.setParameter("to_date", Date.from(request.toDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
        }
    }

    private fun orderBy(request: SearchSalesKpiRequest): String =
        if (request.aggregate) {
            ""
        } else {
            "ORDER BY a.date"
        }

    private fun execute(sql: String): Long {
        val cnn = ds.connection
        cnn.use {
            val stmt = cnn.createStatement()
            stmt.use {
                return stmt.executeUpdate(sql).toLong()
            }
        }
    }
}
