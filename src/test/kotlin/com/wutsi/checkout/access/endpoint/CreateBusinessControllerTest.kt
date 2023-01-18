package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dto.CreateBusinessRequest
import com.wutsi.checkout.access.dto.CreateBusinessResponse
import com.wutsi.enums.BusinessStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/CreateBusinessController.sql"])
class CreateBusinessControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: BusinessRepository

    @Test
    fun create() {
        val request = CreateBusinessRequest(
            accountId = 100,
            currency = "XAF",
            country = "CM",
        )
        val response = rest.postForEntity(url(), request, CreateBusinessResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body!!.businessId
        val business = dao.findById(id)
        assertTrue(business.isPresent)
        assertEquals(request.accountId, business.get().accountId)
        assertEquals(BusinessStatus.ACTIVE, business.get().status)
        assertEquals(request.currency, business.get().currency)
        assertEquals(request.country, business.get().country)
    }

    @Test
    fun createExistingBusiness() {
        val request = CreateBusinessRequest(
            accountId = 200,
            currency = "XAF",
            country = "CM",
        )
        val response = rest.postForEntity(url(), request, CreateBusinessResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val id = response.body!!.businessId
        assertEquals(201, id)

        val business = dao.findById(id)
        assertTrue(business.isPresent)
        assertEquals(request.accountId, business.get().accountId)
        assertEquals(BusinessStatus.ACTIVE, business.get().status)
    }

    private fun url() = "http://localhost:$port/v1/businesses"
}
