package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dto.SearchSalesKpiRequest
import com.wutsi.checkout.access.entity.SalesKpiEntity
import com.wutsi.enums.OrderStatus
import com.wutsi.platform.core.storage.StorageService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.UUID
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.sql.DataSource
import javax.transaction.Transactional

@Service
class SalesKpiService(
    private val em: EntityManager,
    private val ds: DataSource,
    private val storageService: StorageService,
) {
    @Transactional
    fun compute(date: OffsetDateTime): Long {
        val sql =
            """
                INSERT INTO T_KPI_SALES(date, business_fk, product_id, total_orders, total_units, total_value)
                    SELECT date, business_fk, product_id, total_orders, total_units, total_value FROM
                    (
                        SELECT
                                DATE(O.created) AS date,
                                O.business_fk,
                                I.product_id,
                                COUNT(I.product_id) AS total_orders,
                                SUM(I.quantity) AS total_units,
                                SUM(I.total_price) AS  total_value
                            FROM T_ORDER O
                                JOIN T_ORDER_ITEM I ON I.order_fk=O.id
                            WHERE
                                O.created >= ?
                                AND O.status NOT IN (
                                    ${OrderStatus.UNKNOWN.ordinal},
                                    ${OrderStatus.PENDING.ordinal},
                                    ${OrderStatus.EXPIRED.ordinal}
                                )
                            GROUP BY DATE(O.created), O.business_fk, I.product_id
                    ) TMP
                ON DUPLICATE KEY UPDATE total_orders=TMP.total_orders, total_units=TMP.total_units, total_value=TMP.total_value
            """.trimIndent()
        val cnn = ds.connection
        cnn.use {
            val stmt = cnn.prepareStatement(sql)
            stmt.setDate(1, java.sql.Date(Date.from(date.toInstant()).time))
            stmt.use {
                return stmt.executeUpdate().toLong()
            }
        }
    }

    /**
     * Export sales to CSV.
     * The CSV file has the following columns:
     *   - business_id
     *   - product_id
     *   - total_orders
     *   - total_units
     *   - total_sales
     */
    fun export(date: OffsetDateTime): Long {
        val sql = """
            SELECT K.business_fk, K.product_id, SUM(K.total_orders) AS total_orders, SUM(K.total_units) as total_units, SUM(K.total_value) as total_value
            FROM T_KPI_SALES K
            WHERE
                K.product_id IN (SELECT DISTINCT I.product_id FROM T_ORDER_ITEM I JOIN T_ORDER O ON I.order_fk=O.id WHERE O.created >= ?)
            GROUP BY K.business_fk, K.product_id
        """.trimIndent()

        val cnn = ds.connection
        cnn.use {
            val stmt = cnn.prepareStatement(sql)
            stmt.setDate(1, java.sql.Date(Date.from(date.toInstant()).time))
            stmt.use {
                val rs = stmt.executeQuery()
                rs.use {
                    return export(date, rs)
                }
            }
        }
    }

    private fun export(date: OffsetDateTime, rs: ResultSet): Long {
        // Store to file
        val file = File.createTempFile(UUID.randomUUID().toString(), "csv")
        var result: Long = 0
        try {
            val writer: BufferedWriter = Files.newBufferedWriter(file.toPath())
            writer.use {
                val printer = CSVPrinter(
                    writer,
                    CSVFormat.DEFAULT
                        .builder()
                        .setHeader("business_id", "product_id", "total_orders", "total_units", "total_value")
                        .build(),
                )
                printer.use {
                    while (rs.next()) {
                        printer.printRecord(
                            rs.getLong("business_fk"),
                            rs.getLong("product_id"),
                            rs.getLong("total_orders"),
                            rs.getLong("total_units"),
                            rs.getLong("total_value"),
                        )
                        result++
                    }
                    printer.flush()
                }
            }

            // Store file to S3
            val path = "kpi/${date.year}/${date.monthValue}/${date.dayOfMonth}/sales.csv"
            val input = FileInputStream(file)
            input.use {
                storageService.store(path, input, "text/csv", null, "utf-8")
            }
            return result
        } finally {
            file.delete() // Delete  the file
        }
    }

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
}
