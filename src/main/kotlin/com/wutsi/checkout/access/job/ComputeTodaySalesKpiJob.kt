package com.wutsi.checkout.access.job

import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ComputeTodaySalesKpiJob(
    lockManager: CronLockManager,
) : AbstractComputeSalesKpiJob(lockManager) {
    override fun getDate(): LocalDate = LocalDate.now()

    override fun getJobName() = "compute-today-sales-kpi"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-today-sales-kpi.cron}")
    override fun run() {
        super.run()
    }
}
