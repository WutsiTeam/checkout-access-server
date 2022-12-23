package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.service.BusinessService
import com.wutsi.checkout.access.service.SalesKpiService
import com.wutsi.platform.core.cron.AbstractCronJob
import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ComputeTodaySalesKpiJob(
    private val service: SalesKpiService,
    private val businessService: BusinessService,
    lockManager: CronLockManager,
) : AbstractCronJob(lockManager) {
    override fun getJobName() = "compute-today-kpi-sales"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-today-kpi-sales.cron}")
    override fun run() {
        super.run()
    }

    override fun doRun(): Long {
        val date = LocalDate.now()
        val result = service.compute(date)
        if (result > 0) {
            businessService.updateSalesKpi(date)
        }
        return result
    }
}
