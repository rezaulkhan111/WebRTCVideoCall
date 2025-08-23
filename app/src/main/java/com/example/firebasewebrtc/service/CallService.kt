package com.example.firebasewebrtc.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.firebasewebrtc.R
import com.example.firebasewebrtc.presentation.ui.IncomingCallActivity
import com.example.firebasewebrtc.utils.AppConstants

class CallService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createPersistentNotification()
        startForeground(1, notification)

        val callData = intent?.getStringExtra(AppConstants.Common_Transfer_Data)

        val incomingCallIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra(AppConstants.Common_Transfer_Data, callData)

            @Suppress("UseExpressionBody")
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(incomingCallIntent)

        return START_STICKY
    }

    private fun createPersistentNotification(): Notification {
        // ⚠️ You must create a NotificationChannel for Android 8.0+
        val channel = NotificationChannel(
            "call_channel_id",
            "Call Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, "call_channel_id")
            .setContentTitle("Incoming Call")
            .setContentText("Tap to open")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}