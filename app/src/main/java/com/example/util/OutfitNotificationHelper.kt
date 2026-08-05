package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.CalendarEvent

object OutfitNotificationHelper {

    const val CHANNEL_ID = "outfit_reminders_channel"
    private const val CHANNEL_NAME = "Calendar Outfit Reminders"
    private const val CHANNEL_DESC = "Notifications reminding you to check AI outfit suggestions for upcoming events."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerEventOutfitReminder(
        context: Context,
        event: CalendarEvent,
        outfitTitle: String? = null
    ): Boolean {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titleText = "👗 Upcoming Event Outfit: ${event.title}"
        val bodyText = if (!outfitTitle.isNullOrBlank()) {
            "Your AI Outfit '$outfitTitle' is ready for ${event.occasionType} on ${event.date} at ${event.location}!"
        } else {
            "Reminder: Tap to generate your AI-styled outfit for ${event.title} (${event.date} at ${event.time})!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(event.id.hashCode(), builder.build())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun triggerBatchedDailyDigestNotification(
        context: Context,
        upcomingEvents: List<CalendarEvent>
    ): Boolean {
        if (upcomingEvents.isEmpty()) return false
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titleText = "🗓️ Daily Outfit Digest: ${upcomingEvents.size} Events Upcoming!"
        val sb = StringBuilder()
        sb.append("Here is your batched outfit summary digest:\n")
        upcomingEvents.take(4).forEachIndexed { idx, ev ->
            sb.append("${idx + 1}. ").append(ev.title).append(" (").append(ev.date).append(" at ").append(ev.time).append(")\n")
        }
        if (upcomingEvents.size > 4) {
            sb.append("+ ").append(upcomingEvents.size - 4).append(" more events planned.")
        }

        val bodyText = sb.toString().trim()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText("Batched summary for ${upcomingEvents.size} upcoming calendar occasions")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(9999, builder.build())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

