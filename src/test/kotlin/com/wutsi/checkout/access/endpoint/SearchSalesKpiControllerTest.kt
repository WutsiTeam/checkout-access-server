package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dto.SearchSalesKpiRequest
import com.wutsi.checkout.access.dto.SearchSalesKpiResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/SearchSalesKpiController.sql"])
public class SearchSalesKpiControllerTest {
    @LocalServerPort
    public val port: Int = 0

    private val rest = RestTemplate()

    @Test
    public fun byProduct() {
        val request = SearchSalesKpiRequest(
            productId = 100,
        )
        val response = rest.postForEntity(url(), request, SearchSalesKpiResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val kpis = response.body!!.kpis
        assertEquals(3, kpis.size)

        assertEquals(3, kpis[0].totalOrders)
        assertEquals(3, kpis[0].totalUnits)
        assertEquals(3000, kpis[0].totalValue)
        assertEquals(20000, kpis[0].totalViews)

        assertEquals(1, kpis[1].totalOrders)
        assertEquals(2, kpis[1].totalUnits)
        assertEquals(2000, kpis[1].totalValue)
        assertEquals(10000, kpis[1].totalViews)

        assertEquals(1, kpis[2].totalOrders)
        assertEquals(5, kpis[2].totalUnits)
        assertEquals(5000, kpis[2].totalValue)
        assertEquals(30000, kpis[2].totalViews)
    }

    @Test
    public fun byBusiness() {
        val request = SearchSalesKpiRequest(
            businessId = 1,
        )
        val response = rest.postForEntity(url(), request, SearchSalesKpiResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val kpis = response.body!!.kpis
        assertEquals(4, kpis.size)

        assertEquals(11, kpis[0].totalOrders)
        assertEquals(33, kpis[0].totalUnits)
        assertEquals(50000, kpis[0].totalValue)
        assertEquals(30000, kpis[0].totalViews)

        assertEquals(4, kpis[1].totalOrders)
        assertEquals(13, kpis[1].totalUnits)
        assertEquals(8000, kpis[1].totalValue)
        assertEquals(21000, kpis[1].totalViews)

        assertEquals(1, kpis[2].totalOrders)
        assertEquals(2, kpis[2].totalUnits)
        assertEquals(2000, kpis[2].totalValue)
        assertEquals(10000, kpis[2].totalViews)

        assertEquals(1, kpis[3].totalOrders)
        assertEquals(5, kpis[3].totalUnits)
        assertEquals(5000, kpis[3].totalValue)
        assertEquals(30000, kpis[3].totalViews)
    }

    @Test
    public fun aggregateByProduct() {
        val request = SearchSalesKpiRequest(
            productId = 100,
            aggregate = true,
        )
        val response = rest.postForEntity(url(), request, SearchSalesKpiResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val kpis = response.body!!.kpis
        assertEquals(1, kpis.size)

        assertEquals(5, kpis[0].totalOrders)
        assertEquals(10, kpis[0].totalUnits)
        assertEquals(10000, kpis[0].totalValue)
        assertEquals(60000, kpis[0].totalViews)
    }

    private fun url() = "http://localhost:$port/v1/kpis/sales/search"
}
