package com.gramasethu.app.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * SoundUtils plays warning sound and vibration
 * when user is near a SUBMERGED bridge.
 */
object SoundUtils {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Play a loud warning alarm sound
     */
    fun playWarningSound(context: Context) {
        try {
            // Stop any existing sound first
            stopSound()

            // Get the default alarm sound from the phone
            val alarmUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            ) ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = false
                prepare()
                start()
            }

            // Auto stop after 3 seconds
            android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed({ stopSound() }, 3000)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Vibrate the phone in warning pattern
     * Pattern: wait 0ms, vibrate 500ms, wait 200ms, vibrate 500ms
     */
    fun vibrateWarning(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(
                    Context.VIBRATOR_MANAGER_SERVICE
                ) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500),
                        -1  // -1 means don't repeat
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}