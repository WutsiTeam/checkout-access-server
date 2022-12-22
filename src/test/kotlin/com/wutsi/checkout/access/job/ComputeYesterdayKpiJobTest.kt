package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.dao.KpiRepository
import com.wutsi.enums.KpiType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/ComputeYesterdayKpiJob.sql"])
internal class ComputeYesterdayKpiJobTest {
    @Autowired
    private lateinit var job: ComputeYesterdayKpiJob

    @Autowired
    private lateinit var dao: KpiRepository

    @Test
    fun run() {
        job.run()

        val date = LocalDate.now().minusDays(1)

        // Business: Day
        assertBusinessKpiValue(2, 1, KpiType.CUSTOMER_COUNT, date.year, date.monthValue, date.dayOfMonth)
        assertBusinessKpiValue(3, 1, KpiType.ORDER_COUNT, date.year, date.monthValue, date.dayOfMonth)
        assertBusinessKpiValue(8500, 1, KpiType.SALES, date.year, date.monthValue, date.dayOfMonth)

        assertBusinessKpiValue(1, 2, KpiType.CUSTOMER_COUNT, date.year, date.monthValue, date.dayOfMonth)
        assertBusinessKpiValue(1, 2, KpiType.ORDER_COUNT, date.year, date.monthValue, date.dayOfMonth)
        assertBusinessKpiValue(1500, 2, KpiType.SALES, date.year, date.monthValue, date.dayOfMonth)

        // Business: Overall
        assertBusinessKpiValue(12, 1, KpiType.CUSTOMER_COUNT, 0, 0, 0)
        assertBusinessKpiValue(4, 1, KpiType.ORDER_COUNT, 0, 0, 0)
        assertBusinessKpiValue(9500, 1, KpiType.SALES, 0, 0, 0)

        assertBusinessKpiValue(1, 2, KpiType.CUSTOMER_COUNT, 0, 0, 0)
        assertBusinessKpiValue(1, 2, KpiType.ORDER_COUNT, 0, 0, 0)
        assertBusinessKpiValue(1500, 2, KpiType.SALES, 0, 0, 0)

        // Product: Day
        assertProductKpiValue(3, 100, KpiType.ORDER_COUNT, date.year, date.monthValue, date.dayOfMonth)
        assertProductKpiValue(9000, 100, KpiType.SALES, date.year, date.monthValue, date.dayOfMonth)

        // Product Overall
        assertProductKpiValue(3, 100, KpiType.ORDER_COUNT, 0, 0, 0)
        assertProductKpiValue(9000, 100, KpiType.SALES, 0, 0, 0)
    }

    private fun assertBusinessKpiValue(
        expected: Long,
        businessId: Long,
        type: KpiType,
        year: Int,
        month: Int,
        day: Int,
    ) {
        val kpi = dao.findByBusinessIdAndTypeAndYearAndMonthAndDay(businessId, type, year, month, day)
        assertTrue(kpi.isPresent)
        assertEquals(expected, kpi.get().value)
    }

    private fun assertProductKpiValue(
        expected: Long,
        productId: Long,
        type: KpiType,
        year: Int,
        month: Int,
        day: Int,
    ) {
        val kpi = dao.findByProductIdAndTypeAndYearAndMonthAndDay(productId, type, year, month, day)
        assertTrue(kpi.isPresent)
        assertEquals(expected, kpi.get().value)
    }
}
