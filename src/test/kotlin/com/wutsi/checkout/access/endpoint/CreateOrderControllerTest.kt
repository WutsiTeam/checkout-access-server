package com.wutsi.checkout.access.endpoint

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.checkout.access.dao.OrderDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemDiscountRepository
import com.wutsi.checkout.access.dao.OrderItemRepository
import com.wutsi.checkout.access.dao.OrderRepository
import com.wutsi.checkout.access.dto.CreateOrderDiscountRequest
import com.wutsi.checkout.access.dto.CreateOrderItemRequest
import com.wutsi.checkout.access.dto.CreateOrderRequest
import com.wutsi.checkout.access.dto.CreateOrderResponse
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.DiscountType
import com.wutsi.enums.OrderStatus
import com.wutsi.enums.ProductType
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestTemplate
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = ["/db/clean.sql", "/db/CreateOrderController.sql"])
class CreateOrderControllerTest {
    @LocalServerPort
    val port: Int = 0

    private val rest = RestTemplate()

    @Autowired
    private lateinit var dao: OrderRepository

    @Autowired
    private lateinit var itemDao: OrderItemRepository

    @Autowired
    private lateinit var discountDao: OrderDiscountRepository

    @Autowired
    private lateinit var itemDiscountDao: OrderItemDiscountRepository

    @MockBean
    private lateinit var tracingContext: TracingContext

    val deviceId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        doReturn(deviceId).whenever(tracingContext).deviceId()
    }

    @Test
    fun create() {
        val request = CreateOrderRequest(
            businessId = 1,
            customerAccountId = 22,
            customerName = "Ray Sponsible",
            customerEmail = "ray.sponsible@gmail.com",
            deviceType = DeviceType.MOBILE.name,
            channelType = ChannelType.APP.name,
            notes = "This is the notes",
            currency = "XAF",
            expires = OffsetDateTime.now().plusMinutes(45),
            discounts = listOf(
                CreateOrderDiscountRequest(
                    discountId = 111,
                    name = "X-111",
                    amount = 2000,
                    type = DiscountType.COUPON.name,
                ),
            ),
            items = listOf(
                CreateOrderItemRequest(
                    productId = 111,
                    productType = ProductType.DIGITAL_DOWNLOAD.name,
                    unitPrice = 15000,
                    title = "Chemise",
                    pictureUrl = "https://www.img.1/111.png",
                    quantity = 3,
                ),
                CreateOrderItemRequest(
                    productId = 222,
                    productType = ProductType.EVENT.name,
                    unitPrice = 10000,
                    title = "Chemise",
                    pictureUrl = "https://www.img.1/222.png",
                    quantity = 1,
                    discounts = listOf(
                        CreateOrderDiscountRequest(
                            discountId = 222,
                            name = "SPECIAL",
                            amount = 1000,
                            type = DiscountType.SALES.name,
                        ),
                    ),
                ),
            ),
        )
        val response = rest.postForEntity(url(), request, CreateOrderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        assertEquals(OrderStatus.PENDING.name, response.body.orderStatus)

        val orderId = response.body!!.orderId
        val order = dao.findById(orderId).get()

        assertEquals(OrderStatus.PENDING, order.status)
        assertEquals(request.businessId, order.business.id)
        assertEquals(request.customerAccountId, order.customerAccountId)
        assertEquals(request.customerEmail, order.customerEmail)
        assertEquals(request.customerName, order.customerName)
        assertEquals(DeviceType.MOBILE, order.deviceType)
        assertEquals(deviceId, order.deviceId)
        assertEquals(ChannelType.APP, order.channelType)
        assertEquals(request.notes, order.notes)
        assertEquals(request.currency, order.currency)
        assertEquals(3000, order.totalDiscount)
        assertEquals(55000, order.subTotalPrice)
        assertEquals(52000, order.totalPrice)
        assertNotNull(order.expires)
        assertNull(order.expired)
        assertNull(order.cancelled)
        assertEquals(request.items.size, order.itemCount)
        assertEquals(request.items[0].pictureUrl, order.productPictureUrl1)
        assertEquals(request.items[1].pictureUrl, order.productPictureUrl2)
        assertNull(order.productPictureUrl3)

        val discounts = discountDao.findByOrder(order)
        assertEquals(1, discounts.size)
        assertEquals(request.discounts[0].name, discounts[0].name)
        assertEquals(request.discounts[0].amount, discounts[0].amount)
        assertEquals(request.discounts[0].discountId, discounts[0].discountId)
        assertEquals(DiscountType.valueOf(request.discounts[0].type), discounts[0].type)

        val items = itemDao.findByOrder(order)
        assertEquals(2, items.size)

        assertEquals(request.items[0].quantity, items[0].quantity)
        assertEquals(request.items[0].productId, items[0].productId)
        assertEquals(request.items[0].productType, items[0].productType.name)
        assertEquals(request.items[0].unitPrice, items[0].unitPrice)
        assertEquals(request.items[0].title, items[0].title)
        assertEquals(request.items[0].pictureUrl, items[0].pictureUrl)
        assertEquals(45000L, items[0].subTotalPrice)
        assertEquals(0, items[0].totalDiscount)
        assertEquals(45000L, items[0].totalPrice)

        assertEquals(request.items[1].quantity, items[1].quantity)
        assertEquals(request.items[1].productId, items[1].productId)
        assertEquals(request.items[1].productType, items[1].productType.name)
        assertEquals(request.items[1].unitPrice, items[1].unitPrice)
        assertEquals(request.items[1].title, items[1].title)
        assertEquals(request.items[1].pictureUrl, items[1].pictureUrl)
        assertEquals(10000L, items[1].subTotalPrice)
        assertEquals(1000, items[1].totalDiscount)
        assertEquals(9000L, items[1].totalPrice)

        val itemDiscounts = itemDiscountDao.findByOrderItem(items[1])
        assertEquals(1, itemDiscounts.size)
        assertEquals(request.items[1].discounts[0].name, itemDiscounts[0].name)
        assertEquals(request.items[1].discounts[0].amount, itemDiscounts[0].amount)
        assertEquals(request.items[1].discounts[0].discountId, itemDiscounts[0].discountId)
        assertEquals(DiscountType.valueOf(request.items[1].discounts[0].type), itemDiscounts[0].type)
    }

    @Test
    fun free() {
        val request = CreateOrderRequest(
            businessId = 1,
            customerAccountId = 22,
            customerName = "Ray Sponsible",
            customerEmail = "ray.sponsible@gmail.com",
            deviceType = DeviceType.MOBILE.name,
            channelType = ChannelType.APP.name,
            notes = "This is the notes",
            currency = "XAF",
            items = listOf(
                CreateOrderItemRequest(
                    productId = 111,
                    productType = ProductType.PHYSICAL_PRODUCT.name,
                    unitPrice = 0,
                    title = "Chemise",
                    pictureUrl = "https://www.img.1/111.png",
                    quantity = 3,
                ),
            ),
            expires = OffsetDateTime.now().plusMinutes(30),
        )
        val response = rest.postForEntity(url(), request, CreateOrderResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)

        assertEquals(OrderStatus.OPENED.name, response.body.orderStatus)

        val orderId = response.body!!.orderId
        val order = dao.findById(orderId).get()

        assertEquals(OrderStatus.OPENED, order.status)
        assertEquals(request.businessId, order.business.id)
        assertEquals(request.customerAccountId, order.customerAccountId)
        assertEquals(request.customerEmail, order.customerEmail)
        assertEquals(request.customerName, order.customerName)
        assertEquals(DeviceType.MOBILE, order.deviceType)
        assertEquals(deviceId, order.deviceId)
        assertEquals(ChannelType.APP, order.channelType)
        assertEquals(request.notes, order.notes)
        assertEquals(request.currency, order.currency)
        assertEquals(0, order.totalDiscount)
        assertEquals(0, order.subTotalPrice)
        assertEquals(0, order.totalPrice)
        assertEquals(request.items.size, order.itemCount)
        assertEquals(request.items[0].pictureUrl, order.productPictureUrl1)
        assertNull(order.productPictureUrl2)
        assertNull(order.productPictureUrl3)

        val items = itemDao.findByOrder(order)
        assertEquals(1, items.size)

        assertEquals(request.items[0].quantity, items[0].quantity)
        assertEquals(request.items[0].productId, items[0].productId)
        assertEquals(request.items[0].productType, items[0].productType.name)
        assertEquals(request.items[0].unitPrice, items[0].unitPrice)
        assertEquals(request.items[0].title, items[0].title)
        assertEquals(request.items[0].pictureUrl, items[0].pictureUrl)
        assertEquals(0, items[0].subTotalPrice)
        assertEquals(0, items[0].totalDiscount)
        assertEquals(0, items[0].totalPrice)
    }

    private fun url() = "http://localhost:$port/v1/orders"
}
