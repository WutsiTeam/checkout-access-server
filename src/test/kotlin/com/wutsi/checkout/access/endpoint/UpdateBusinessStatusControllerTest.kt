package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dto.CreateBusinessResponse
import com.wutsi.checkout.access.dto.UpdateBusinessStatusRequest
import com.wutsi.enums.BusinessStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/UpdateBusinessStatusController.sql"])
class UpdateBusinessStatusControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: BusinessRepository

    @Test
    fun suspend() {
        val request = UpdateBusinessStatusRequest(
            status = BusinessStatus.SUSPENDED.name
        )
        val response = rest.postForEntity(url(100), request, CreateBusinessResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val business = dao.findById(100)
        assertEquals(BusinessStatus.SUSPENDED, business.get().status)
        assertNotNull(business.get().suspended)
    }

    @Test
    fun sameStatus() {
        val now = System.currentTimeMillis()
        Thread.sleep(1000)

        val request = UpdateBusinessStatusRequest(
            status = BusinessStatus.UNDER_REVIEW.name
        )
        val response = rest.postForEntity(url(200), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val business = dao.findById(200)
        assertTrue(business.get().updated.before(Date(now)))
    }

    private fun url(id: Long) = "http://localhost:$port/v1/businesses/$id/status"
}
