package com.skillbridge.ai.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skillbridge.ai.domain.repository.CopilotRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalTime

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun copilotRepository(): CopilotRepository
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerEntryPoint::class.java
        )
        val repo = entryPoint.copilotRepository()

        // 1. Fetch settings and context
        val settingsResult = repo.getReminderSettings()
        val contextResult = repo.getCopilotContext()

        if (settingsResult.isFailure || contextResult.isFailure) {
            return Result.retry()
        }

        val settings = settingsResult.getOrThrow()
        val copilotContext = contextResult.getOrThrow()

        // 2. Check if reminders are enabled
        if (!settings.notificationsEnabled) {
            return Result.success()
        }

        // 3. Check quiet hours restriction
        val now = LocalTime.now()
        val startStr = settings.quietHoursStart.takeIf { it.isNotBlank() } ?: "22:00"
        val endStr = settings.quietHoursEnd.takeIf { it.isNotBlank() } ?: "07:00"

        try {
            val start = LocalTime.parse(startStr)
            val end = LocalTime.parse(endStr)
            val isInQuietHours = if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                now.isAfter(start) || now.isBefore(end)
            }
            if (isInQuietHours) {
                return Result.success() // Do not notify during quiet hours
            }
        } catch (e: Exception) {
            System.err.println("Quiet hours check failed: ${e.message}")
        }

        // 4. Send notification if missed tasks exist
        val missed = copilotContext.missedTasks
        if (missed.isNotEmpty()) {
            val task = missed.first()
            sendReminderNotification(
                title = "SkillBridge Objective Pending 🚀",
                message = "Don't break your streak! Today's goal is: ${task.title}"
            )
        }

        return Result.success()
    }

    private fun sendReminderNotification(title: String, message: String) {
        val channelId = "skillbridge_daily_reminders"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when daily learning milestones are pending"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deep link redirection back to main app activity
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(101, builder.build())
    }
}
