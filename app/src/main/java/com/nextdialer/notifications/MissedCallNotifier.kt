package com.nextdialer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nextdialer.MainActivity
import com.nextdialer.R

/**
 * Posts a single summary notification for unread missed calls. The notification is also what
 * drives the launcher badge on Android 8+ (the system derives the badge from the notification
 * number), so no third-party badge library is needed.
 *
 * Background limitation: missed calls are discovered through the call-log ContentObserver while
 * the process is alive. Reliable background notification needs the default-dialer role or a
 * call-screening service, which is planned for a later checkpoint.
 */
object MissedCallNotifier {

    private const val CHANNEL_ID = "missed_calls"
    private const val NOTIFICATION_ID = 1001

    fun update(context: Context, count: Int, enabled: Boolean) {
        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        if (!enabled || count <= 0) {
            manager.cancel(NOTIFICATION_ID)
            return
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val text = if (count == 1) {
            "1 missed call"
        } else {
            "$count missed calls"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_missed_call)
            .setContentTitle(text)
            .setContentText("Open NEXT Dialer to see who called.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setNumber(count)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Missed calls",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Summary of missed calls"
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }
}
