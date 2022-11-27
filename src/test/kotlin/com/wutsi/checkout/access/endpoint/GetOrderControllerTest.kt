package com.wutsi.checkout.access.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.access.dto.GetOrderResponse
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.DiscountType
import com.wutsi.enums.OfferType
import com.wutsi.enums.OrderStatus
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/GetOrderController.sql"])
class GetOrderControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun get() {
        val response = rest.getForEntity(url("100-AEF01-1111"), GetOrderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        val order = response.body!!.order
        assertEquals("1111", order.shortId)
        assertEquals(1L, order.businessId)
        assertEquals(11L, order.customerId)
        assertEquals("Ray Sponsible", order.customerName)
        assertEquals("ray.sponsible@gmail.com", order.customerEmail)
        assertEquals("0000-1111", order.deviceId)
        assertEquals(DeviceType.MOBILE.name, order.deviceType)
        assertEquals(ChannelType.EMAIL.name, order.channelType)
        assertEquals(OrderStatus.CANCELLED.name, order.status)
        assertEquals(5000L, order.subTotalPrice)
        assertEquals(1000, order.totalDiscount)
        assertEquals(4000L, order.totalPrice)
        assertEquals(1500, order.totalPaid)
        assertEquals(2500, order.balance)
        assertEquals("Thanks", order.notes)
        assertEquals("Not available", order.cancellationReason)
        assertNotNull(order.cancelled)
        assertNotNull(order.created)
        assertNotNull(order.updated)
        assertNull(order.closed)

        assertEquals(1, order.discounts.size)
        assertEquals("C-100", order.discounts[0].code)
        assertEquals(100L, order.discounts[0].amount)
        assertEquals(2, order.discounts[0].rate)
        assertEquals(DiscountType.MERCHANT.name, order.discounts[0].type)

        assertEquals(2, order.items.size)
        assertEquals(555L, order.items[0].offerId)
        assertEquals(OfferType.PRODUCT.name, order.items[0].offerType)
        assertEquals(2, order.items[0].quantity)
        assertEquals(1500L, order.items[0].unitPrice)
        assertEquals(3000L, order.items[0].subTotalPrice)
        assertEquals(900L, order.items[0].totalDiscount)
        assertEquals(2100L, order.items[0].totalPrice)

        assertEquals(2, order.items[0].discounts.size)
        assertEquals("MERCHANT", order.items[0].discounts[0].code)
        assertEquals(500L, order.items[0].discounts[0].amount)
        assertEquals(10, order.items[0].discounts[0].rate)
        assertEquals(DiscountType.MERCHANT.name, order.items[0].discounts[0].type)

        assertEquals("MOBILE", order.items[0].discounts[1].code)
        assertEquals(400, order.items[0].discounts[1].amount)
        assertEquals(8, order.items[0].discounts[1].rate)
        assertEquals(DiscountType.DYNAMIC.name, order.items[0].discounts[1].type)

        assertEquals(666L, order.items[1].offerId)
        assertEquals(OfferType.PRODUCT.name, order.items[1].offerType)
        assertEquals(1, order.items[1].quantity)
        assertEquals(2000L, order.items[1].unitPrice)
        assertEquals(2000L, order.items[1].subTotalPrice)
        assertEquals(0L, order.items[1].totalDiscount)
        assertEquals(2000L, order.items[1].totalPrice)
        assertTrue(order.items[1].discounts.isEmpty())
    }

    @Test
    fun notFound() {
        val ex = assertThrows<HttpClientErrorException> {
            rest.getForEntity(url("0000"), GetOrderResponse::class.java)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)

        val response = ObjectMapper().readValue(ex.responseBodyAsString, ErrorResponse::class.java)
        assertEquals(ErrorURN.ORDER_NOT_FOUND.urn, response.error.code)
    }

    private fun url(id: String) = "http://localhost:$port/v1/orders/$id"
}
