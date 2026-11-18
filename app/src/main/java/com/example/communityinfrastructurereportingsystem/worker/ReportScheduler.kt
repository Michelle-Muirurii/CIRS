package com.example.communityinfrastructurereportingsystem.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// Starts the background checks (Lesson 12)
object ReportScheduler {

    fun start(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Constraint: do not run when the battery is low
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        // 1. Repeats about every 15 minutes (the minimum WorkManager allows).
        //    KEEP: if this work is already scheduled, leave it alone (no duplicates)
        val periodicWork = PeriodicWorkRequestBuilder<StatusCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "report_status_check", ExistingPeriodicWorkPolicy.KEEP, periodicWork
        )

        // 2. Also check once, right now (so changes show up as soon as a resident opens the app)
        workManager.enqueueUniqueWork(
            "report_status_check_now",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<StatusCheckWorker>().build()
        )
    }
}