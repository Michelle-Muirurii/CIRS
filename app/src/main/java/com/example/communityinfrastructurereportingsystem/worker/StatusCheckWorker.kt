package com.example.communityinfrastructurereportingsystem.worker


import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.communityinfrastructurereportingsystem.data.local.CirsDatabase
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.UserRole
import com.example.communityinfrastructurereportingsystem.data.repository.ReportRepository
import com.example.communityinfrastructurereportingsystem.ui.main.MainActivity

// A Worker runs in the background, even when the app is closed (Lesson 12)
class StatusCheckWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    // doWork() is the job. Its Result tells WorkManager how it went
    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)

        // Only residents get status notifications
        if (!session.isLoggedIn() || session.role != UserRole.RESIDENT) {
            return Result.success()
        }

        return try {
            val repository = ReportRepository(
                CirsDatabase.getDatabase(applicationContext).reportDao()
            )
            val reports = repository.reportsByUserOnce(session.userId)

            // The statuses we saw LAST time: "report id -> status"
            val saved = applicationContext.getSharedPreferences("status_snapshot", Context.MODE_PRIVATE)

            for (report in reports) {
                val key = "status_${report.id}"
                val lastKnown = saved.getString(key, null)

                // lastKnown == null: a report we have not seen yet (nothing changed)
                if (lastKnown != null && lastKnown != report.status) {
                    showNotification(report)
                }
                saved.edit().putString(key, report.status).apply()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()   // something failed: ask WorkManager to try again later
        }
    }

    @SuppressLint("MissingPermission")   // we check the permission ourselves below
    private fun showNotification(report: Report) {
        val context = applicationContext

        // Android 13+ needs the user's permission to show notifications
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Android 8+ needs a "channel" (a category the user can mute)
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).setName("Report updates").build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)

        // Tapping the notification opens the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Report update")
            .setContentText("\"${report.title}\" is now ${report.status}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // report.id as the notification id: one notification per report
        NotificationManagerCompat.from(context).notify(report.id, notification)
    }

    companion object {
        private const val CHANNEL_ID = "report_updates"
    }
}