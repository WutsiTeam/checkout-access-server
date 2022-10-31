package com.wutsi.checkout.access.endpoint

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchPaymentMethodControllerTest {
    @LocalServerPort
    public val port: Int = 0

    @Test
    public fun invoke() {
    }
}
