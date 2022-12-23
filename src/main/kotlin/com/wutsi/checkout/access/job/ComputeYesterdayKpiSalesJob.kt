package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.service.KpiSalesService
import com.wutsi.platform.core.cron.AbstractCronJob
import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ComputeYesterdayKpiSalesJob(
    private val service: KpiSalesService,
    lockManager: CronLockManager,
) : AbstractCronJob(lockManager) {
    override fun getJobName() = "compute-yesterday-kpi-sales"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-yesterday-kpi-sales.cron}")
    override fun run() {
        super.run()
    }

    override fun doRun(): Long {
        val date = LocalDate.now().minusDays(1)
        return service.compute(date)
    }
}
