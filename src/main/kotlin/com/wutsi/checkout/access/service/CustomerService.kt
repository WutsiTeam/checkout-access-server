package com.wutsi.checkout.access.service

import com.wutsi.enums.OrderStatus
import org.springframework.stereotype.Service
import javax.sql.DataSource
import javax.transaction.Transactional

@Service
class CustomerService(
    private val ds: DataSource,
) {
    /**
     * Create customers from the past hour orders
     */
    @Transactional
    fun create(): Int {
        val sql = """
            INSERT INTO T_CUSTOMER(business_fk, email)
                SELECT DISTINCT business_fk, customer_email FROM T_ORDER WHERE
                    created BETWEEN date_add(now(), interval -1 day) and now() AND
                    status NOT IN (
                        ${OrderStatus.UNKNOWN.ordinal},
                        ${OrderStatus.PENDING.ordinal},
                        ${OrderStatus.EXPIRED.ordinal}
                    )
            ON DUPLICATE KEY UPDATE email=email;
        """.trimIndent()

        val cnn = ds.connection
        cnn.use {
            val stmt = cnn.createStatement()
            stmt.use {
                return stmt.executeUpdate(sql)
            }
        }
    }
}
