package com.wutsi.checkout.access.service

import com.wutsi.enums.OrderStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.sql.DataSource
import javax.transaction.Transactional

@Service
class KpiSalesService(private val ds: DataSource) {
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
