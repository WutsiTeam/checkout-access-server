package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dao.CustomerRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/CreateCustomerJob.sql"])
internal class CreateCustomerJobTest {
    @Autowired
    private lateinit var job: CreateCustomerJob

    @Autowired
    private lateinit var businessDao: BusinessRepository

    @Autowired
    private lateinit var dao: CustomerRepository

    @Test
    fun run() {
        job.run()

        val customers1 = dao.findByBusiness(businessDao.findById(1L).get())
        assertEquals(4, customers1.size)

        val customers2 = dao.findByBusiness(businessDao.findById(2L).get())
        assertEquals(2, customers2.size)
    }
}
