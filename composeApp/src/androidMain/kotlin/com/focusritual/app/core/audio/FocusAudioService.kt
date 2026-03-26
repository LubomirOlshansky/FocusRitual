package com.focusritual.app.core.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class FocusAudioService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("FocusRitual")
            .setContentText("Focus session is running")
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel(CHANNEL_ID, "Focus Session Audio", NotificationManager.IMPORTANCE_LOW)
                .also { manager.createNotificationChannel(it) }
        }
    }

    companion object {
        private const val CHANNEL_ID = "focus_audio"
        private const val NOTIFICATION_ID = 1
    }
}
