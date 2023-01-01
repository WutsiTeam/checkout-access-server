package com.wutsi.checkout.access.job

import com.wutsi.checkout.access.service.BusinessService
import com.wutsi.checkout.access.service.SalesKpiService
import com.wutsi.platform.core.cron.AbstractCronJob
import com.wutsi.platform.core.cron.CronLockManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ComputeSalesKpiJob(
    private val service: SalesKpiService,
    private val businessService: BusinessService,
    lockManager: CronLockManager,

    @Value("\${wutsi.application.jobs.compute-sales-kpi.date-threshold-hours}") private val dateThreshold: Long,
) : AbstractCronJob(lockManager) {
    override fun getJobName() = "compute-sales-kpi"

    @Scheduled(cron = "\${wutsi.application.jobs.compute-sales-kpi.cron}")
    override fun run() {
        super.run()
    }

    override fun doRun(): Long {
        // The job runs every hour - compute KPIs for orders in the past xx hours
        val date = OffsetDateTime.now().minusHours(dateThreshold)

        // Compute the KPIs
        val result = service.computeFromOrders(date) + service.importViews(date)
        if (result > 0) {
            // Update business KPI
            businessService.updateSalesKpi(date)

            // Export the KPI to storage
            service.export(date)
        }
        return result
    }
}
