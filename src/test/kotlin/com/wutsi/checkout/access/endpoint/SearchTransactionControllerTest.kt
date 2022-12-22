package com.wutsi.checkout.access.endpoint

import com.wutsi.checkout.access.dto.SearchTransactionRequest
import com.wutsi.checkout.access.dto.SearchTransactionResponse
import com.wutsi.enums.TransactionType
import com.wutsi.platform.payment.core.Status
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/SearchTransactionController.sql"])
class SearchTransactionControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun searchByCustomerId() {
        // WHEN
        val request = SearchTransactionRequest(
            customerAccountId = 100L,
        )
        val response = rest.postForEntity(url(), request, SearchTransactionResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val txs = response.body!!.transactions
        assertEquals(2, txs.size)
        assertEquals(listOf("tx-100", "tx-101"), txs.map { it.id })
    }

    @Test
    fun searchByBusinessId() {
        // WHEN
        val request = SearchTransactionRequest(
            businessId = 3L,
        )
        val response = rest.postForEntity(url(), request, SearchTransactionResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val txs = response.body!!.transactions
        assertEquals(1, txs.size)
        assertEquals(listOf("tx-000"), txs.map { it.id })
    }

    @Test
    fun searchByType() {
        // WHEN
        val request = SearchTransactionRequest(
            type = TransactionType.CHARGE.name,
        )
        val response = rest.postForEntity(url(), request, SearchTransactionResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val txs = response.body!!.transactions
        assertEquals(3, txs.size)
        assertEquals(listOf("tx-100", "tx-101", "tx-201"), txs.map { it.id })
    }

    @Test
    fun searchByOrderId() {
        // WHEN
        val request = SearchTransactionRequest(
            orderId = "order-100",
        )
        val response = rest.postForEntity(url(), request, SearchTransactionResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val txs = response.body!!.transactions
        assertEquals(2, txs.size)
        assertEquals(listOf("tx-100", "tx-101"), txs.map { it.id })
    }

    @Test
    fun searchByStatus() {
        // WHEN
        val request = SearchTransactionRequest(
            status = listOf(Status.SUCCESSFUL.name, Status.PENDING.name),
        )
        val response = rest.postForEntity(url(), request, SearchTransactionResponse::class.java)

        // THEN
        assertEquals(HttpStatus.OK, response.statusCode)

        val txs = response.body!!.transactions
        assertEquals(3, txs.size)
        assertEquals(listOf("tx-100", "tx-101", "tx-201"), txs.map { it.id })
    }

    private fun url() = "http://localhost:$port/v1/transactions/search"
}
