package com.gramasethu.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gramasethu.app.MainActivity

/**
 * NotificationUtils creates and shows push notifications
 * for bridge alerts.
 */
object NotificationUtils {

    private const val CHANNEL_ID = "bridge_alerts"
    private const val CHANNEL_NAME = "Bridge Alerts"
    private const val NOTIFICATION_ID = 1001

    /**
     * Create notification channel (required for Android 8+)
     * Call this when app starts
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH  // High = shows as popup
        ).apply {
            description = "Alerts for submerged or damaged bridges nearby"
            enableVibration(true)
            enableLights(true)
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Show a bridge danger notification
     */
    fun showBridgeAlert(
        context: Context,
        bridgeName: String,
        status: String,
        distance: String = ""
    ) {
        // When user taps notification, open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (status) {
            "SUBMERGED" -> "🚨 BRIDGE SUBMERGED NEARBY!"
            "DAMAGED" -> "⚠️ Damaged Bridge Nearby"
            else -> "🌉 Bridge Status Update"
        }

        val message = buildString {
            append("$bridgeName is $status")
            if (distance.isNotEmpty()) append(" • $distance away")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nOpen the app to see alternate routes.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show monsoon warning notification
     */
    fun showMonsoonAlert(context: Context, submergedCount: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🌧️ Monsoon Alert!")
            .setContentText("$submergedCount bridges are now SUBMERGED in your area!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, notification)
    }
}