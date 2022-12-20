package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dao.BusinessRepository
import com.wutsi.checkout.access.dto.UpdateOrderStatusRequest
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
public class UpdateBusinessStatusControllerTest {
    @LocalServerPort
    public val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: BusinessRepository

    @Test
    fun suspended() {
        val request = UpdateOrderStatusRequest(
            status = BusinessStatus.INACTIVE.name,
        )
        val response = rest.postForEntity(url(100), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val businessEntity = dao.findById(100).get()
        assertEquals(BusinessStatus.INACTIVE, businessEntity.status)
        assertNotNull(businessEntity.deactivated)
    }

    @Test
    fun underReview() {
        val request = UpdateOrderStatusRequest(
            status = BusinessStatus.UNDER_REVIEW.name,
        )
        val response = rest.postForEntity(url(100), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val businessEntity = dao.findById(100).get()
        assertEquals(BusinessStatus.UNDER_REVIEW, businessEntity.status)
    }

    @Test
    fun active() {
        val request = UpdateOrderStatusRequest(
            status = BusinessStatus.ACTIVE.name,
        )
        val response = rest.postForEntity(url(200), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val businessEntity = dao.findById(200).get()
        assertEquals(BusinessStatus.ACTIVE, businessEntity.status)
    }

    @Test
    fun sameStatus() {
        val now = System.currentTimeMillis()

        Thread.sleep(1000)
        val request = UpdateOrderStatusRequest(
            status = BusinessStatus.INACTIVE.name,
        )
        val response = rest.postForEntity(url(300), request, Any::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val businessEntity = dao.findById(300).get()
        assertEquals(BusinessStatus.INACTIVE, businessEntity.status)
        assertTrue(businessEntity.updated.before(Date(now)))
        assertTrue(businessEntity.deactivated?.before(Date(now)) == true)
    }

    private fun url(id: Long) = "http://localhost:$port/v1/businesses/$id/status"
}
