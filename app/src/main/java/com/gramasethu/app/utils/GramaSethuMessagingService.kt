package com.gramasethu.app.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messages (push notifications)
 * when app is in background or foreground.
 */
class GramaSethuMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get data from the notification
        val bridgeName = remoteMessage.data["bridgeName"] ?: "A bridge"
        val status = remoteMessage.data["status"] ?: "SUBMERGED"
        val distance = remoteMessage.data["distance"] ?: ""

        // Show the notification to the user
        NotificationUtils.showBridgeAlert(
            context = applicationContext,
            bridgeName = bridgeName,
            status = status,
            distance = distance
        )

        // Play warning sound if bridge is submerged
        if (status == "SUBMERGED") {
            SoundUtils.playWarningSound(applicationContext)
            SoundUtils.vibrateWarning(applicationContext)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refreshed — in production, save to Firestore
    }
}