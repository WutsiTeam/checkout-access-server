package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.service.CustomerService
import com.wutsi.platform.core.cron.AbstractCronJob
import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CreateCustomerJob(
    private val service: CustomerService,
    lockManager: CronLockManager,
) : AbstractCronJob(lockManager) {
    override fun getJobName() = "create-customer"

    @Scheduled(cron = "\${wutsi.application.jobs.create-customer.cron}")
    override fun run() {
        super.run()
    }

    override fun doRun(): Long {
        return service.create().toLong()
    }
}
