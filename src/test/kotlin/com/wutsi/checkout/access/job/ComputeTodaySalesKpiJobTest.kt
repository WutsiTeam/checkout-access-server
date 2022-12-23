package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.SalesKpiRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/ComputeTodaySalesKpiJob.sql"])
internal class ComputeTodaySalesKpiJobTest {
    @Autowired
    private lateinit var job: ComputeTodaySalesKpiJob

    @Autowired
    private lateinit var dao: SalesKpiRepository

    @Autowired
    private lateinit var businessDao: BusinessRepository

    @Test
    fun run() {
        job.run()

        val date = LocalDate.now()
        assertKpi(3, 6, 9000, 1, 100, date)
        assertKpi(1, 1, 500, 1, 101, date)
        assertKpi(1, 1, 1500, 2, 200, date)

        val business1 = businessDao.findById(1).get()
        assertEquals(4, business1.totalOrders)
        assertEquals(7, business1.totalUnits)
        assertEquals(9500, business1.totalValue)

        val business2 = businessDao.findById(2).get()
        assertEquals(1, business2.totalOrders)
        assertEquals(1, business2.totalUnits)
        assertEquals(1500, business2.totalValue)
    }

    private fun assertKpi(
        totalOrders: Long,
        totalUnits: Long,
        totalValue: Long,
        businessId: Long,
        productId: Long,
        date: LocalDate,
    ) {
        val kpi = dao.findByBusinessAndProductIdAndDate(
            business = businessDao.findById(businessId).get(),
            productId = productId,
            date = Date.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant()),
        )
        assertEquals(totalOrders, kpi.get().totalOrders)
        assertEquals(totalUnits, kpi.get().totalUnits)
        assertEquals(totalValue, kpi.get().totalValue)
    }
}
