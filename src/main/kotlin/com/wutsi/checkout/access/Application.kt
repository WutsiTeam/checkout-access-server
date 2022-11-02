package com.wutsi.checkout.access

import com.wutsi.platform.core.WutsiApplication
import com.wutsi.platform.payment.EnableWutsiPayment
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@WutsiApplication
@EnableWutsiPayment
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Application

public fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}
