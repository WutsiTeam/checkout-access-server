package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.CustomerRepository
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
@Sql(value = ["/db/clean.sql", "/db/ComputeYesterdaySalesKpiJob.sql"])
internal class ComputeYesterdayKpiSalesJobTest {
    @Autowired
    private lateinit var job: ComputeYesterdayKpiSalesJob

    @Autowired
    private lateinit var dao: SalesKpiRepository

    @Autowired
    private lateinit var businessDao: BusinessRepository

    @Autowired
    private lateinit var customerDao: CustomerRepository

    @Test
    fun run() {
        job.run()

        val date = LocalDate.now().minusDays(1)
        assertKpi(1, 3, 4500, 1, 10, 100, date)
        assertKpi(1, 1, 500, 1, 10, 101, date)
        assertKpi(2, 3, 4500, 1, 11, 100, date)
        assertKpi(1, 1, 1500, 2, 20, 200, date)
    }

    private fun assertKpi(
        totalOrders: Long,
        totalUnits: Long,
        totalValue: Long,
        businessId: Long,
        customerId: Long,
        productId: Long,
        date: LocalDate,
    ) {
        val kpi = dao.findByBusinessAndCustomerAndProductIdAndDate(
            business = businessDao.findById(businessId).get(),
            customer = customerDao.findById(customerId).get(),
            productId = productId,
            date = Date.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant()),
        )
        assertEquals(totalOrders, kpi.get().totalOrders)
        assertEquals(totalUnits, kpi.get().totalUnits)
        assertEquals(totalValue, kpi.get().totalValue)
    }
}
