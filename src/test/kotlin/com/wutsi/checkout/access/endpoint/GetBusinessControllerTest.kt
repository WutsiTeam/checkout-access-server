package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dto.GetBusinessResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.BusinessStatus
import com.wutsi.platform.core.error.ErrorResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/GetBusinessController.sql"])
class GetBusinessControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun get() {
        val response = rest.getForEntity(url(100), GetBusinessResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val business = response.body!!.business
        assertEquals(100L, business.accountId)
        assertEquals(BusinessStatus.ACTIVE.name, business.status)
        assertEquals("XAF", business.currency)
        assertEquals(100000L, business.balance)
        assertEquals("CM", business.country)
        assertEquals(30, business.totalOrders)
        assertEquals(150000, business.totalSales)
        assertEquals(2000000, business.totalViews)
        assertEquals(90000L, business.cashoutBalance)
    }

    @Test
    fun notFound() {
        val ex = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url(99999), GetBusinessResponse::class.java)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.BUSINESS_NOT_FOUND.urn, response.error.code)
    }

    private fun url(id: Long) = "http://localhost:$port/v1/businesses/$id"
}
