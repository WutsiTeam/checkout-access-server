package com.wutsi.checkout.access.job

import com.amazonaws.util.IOUtils
import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.SalesKpiRepository
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    @Value("\${wutsi.platform.storage.local.directory}")
    private lateinit var storageDirectory: String

    @Autowired
    private lateinit var storage: StorageService

    @BeforeEach
    fun setUp() {
        File(storageDirectory).deleteRecursively()
    }

    @Test
    fun run() {
        // GIVEN
        val csv = """
            product_id,total_views,business_id
            100,10,1
            101,11,1
            200,20,2
            201,50,2
            99999,99,-1
        """.trimIndent()
        val today = LocalDate.now()
        val path = "kpi/" + today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/views.csv"
        storage.store(path, ByteArrayInputStream(csv.toByteArray()))

        // WEN
        job.run()

        // THEN
        assertKpi(3, 6, 9000, 10, 1, 100, today)
        assertKpi(1, 1, 500, 11, 1, 101, today)
        assertKpi(1, 1, 1500, 20, 2, 200, today)
        assertKpi(0, 0, 0, 50, 2, 201, today)

        val business1 = businessDao.findById(1).get()
        assertEquals(4, business1.totalOrders)
        assertEquals(9500, business1.totalSales)
        assertEquals(21, business1.totalViews)

        val business2 = businessDao.findById(2).get()
        assertEquals(1, business2.totalOrders)
        assertEquals(1500, business2.totalSales)
        assertEquals(70, business2.totalViews)

        val input =
            FileInputStream(File("$storageDirectory/kpi/" + today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/sales.csv"))
        input.use {
            assertEquals(
                """
                    business_id,product_id,total_orders,total_units,total_sales,total_views
                    1,100,3,6,9000,10
                    1,101,1,1,500,11
                    2,200,1,1,1500,20
                """.trimIndent(),
                IOUtils.toString(input).trimIndent(),
            )
        }
    }

    private fun assertKpi(
        totalOrders: Long,
        totalUnits: Long,
        totalValue: Long,
        totalViews: Long,
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
        assertEquals(totalViews, kpi.get().totalViews)
    }
}
