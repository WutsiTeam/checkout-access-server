package com.wutsi.checkout.access.job

import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ComputeYesterdaySalesKpiJob(
    lockManager: CronLockManager,
) : AbstractComputeSalesKpiJob(lockManager) {
    override fun getDate(): LocalDate = LocalDate.now().minusDays(1)

    override fun getJobName() = "compute-yesterday-sales-kpi"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-yesterday-sales-kpi.cron}")
    override fun run() {
        super.run()
    }
}
