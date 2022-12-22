package com.wutsi.checkout.access.service

import com.wutsi.enums.KpiType
import com.wutsi.enums.OrderStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.sql.DataSource
import javax.transaction.Transactional

@Service
class KpiService(private val ds: DataSource) {
    @Transactional
    fun dailyCustomersByBusiness(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT YEAR(C.created) AS year, MONTH(C.created) AS month, DAY(C.created) as day, C.business_fk AS business_id, -1 AS product_id, ${KpiType.CUSTOMER_COUNT.ordinal} AS type, COUNT(id) AS value
                            FROM T_CUSTOMER C
                            WHERE DATE(C.created)='$date'
                            GROUP BY YEAR(C.created), MONTH(C.created), DAY(C.created), C.business_fk
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    @Transactional
    fun dailyOrdersByBusiness(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT YEAR(O.created) AS year, MONTH(O.created) AS month, DAY(O.created) as day, O.business_fk AS business_id, -1 AS product_id, ${KpiType.ORDER_COUNT.ordinal} AS type, COUNT(id) AS value
                            FROM T_ORDER O
                            WHERE ${where(date)}
                            GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    @Transactional
    fun dailySalesByBusiness(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT YEAR(O.created) AS year, MONTH(O.created) AS month, DAY(O.created) as day, O.business_fk AS business_id, -1 AS product_id, ${KpiType.SALES.ordinal} AS type, SUM(total_price) AS value
                            FROM T_ORDER O
                            WHERE ${where(date)}
                            GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    @Transactional
    fun dailyOrdersByProduct(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT YEAR(O.created) AS year, MONTH(O.created) AS month, DAY(O.created) AS day, -1 AS business_id, I.product_id, ${KpiType.ORDER_COUNT.ordinal} AS type, COUNT(product_id) AS value
                            FROM T_ORDER O JOIN T_ORDER_ITEM I ON O.id=I.order_fk
                            WHERE ${where(date)}
                            GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    @Transactional
    fun dailySalesByProduct(date: LocalDate): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT YEAR(O.created) AS year, MONTH(O.created) AS month, DAY(O.created) AS day, -1 AS business_id, I.product_id, ${KpiType.SALES.ordinal} AS type, SUM(I.total_price) AS value
                            FROM T_ORDER O JOIN T_ORDER_ITEM I ON O.id=I.order_fk
                            WHERE ${where(date)}
                            GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    fun aggregateOverall(): Long =
        execute(
            """
                INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
                    SELECT year, month, day, business_id, product_id, type, value FROM
                    (
                        SELECT 0 AS year, 0 AS month, 0 AS day, business_id, product_id, type, SUM(value) AS value
                            FROM T_KPI
                            WHERE year>0 AND month>0 AND day>0
                            GROUP BY business_id, product_id, type
                    ) TMP
                ON DUPLICATE KEY UPDATE value= TMP.value
            """.trimIndent(),
        )

    fun where(date: LocalDate): String =
        """
            DATE(O.created)='$date'
            AND status NOT IN (
                ${OrderStatus.UNKNOWN.ordinal},
                ${OrderStatus.PENDING.ordinal},
                ${OrderStatus.EXPIRED.ordinal}
            )
        """.trimIndent()

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
