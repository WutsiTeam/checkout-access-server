package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.service.KpiService
import com.wutsi.platform.core.cron.AbstractCronJob
import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class ComputeYesterdayKpiJob(
    private val service: KpiService,
    lockManager: CronLockManager,
) : AbstractCronJob(lockManager) {
    override fun getJobName() = "compute-yesterday-kpi"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-yesterday-kpi.cron}")
    override fun run() {
        super.run()
    }

    override fun doRun(): Long {
        val date = LocalDate.now(ZoneOffset.UTC).minusDays(1)
        return service.dailyOrdersByBusiness(date) +
            service.dailyCustomersByBusiness(date) +
            service.dailySalesByBusiness(date) +
            service.dailyOrdersByProduct(date) +
            service.dailySalesByProduct(date) +
            service.aggregateOverall()
    }
}
