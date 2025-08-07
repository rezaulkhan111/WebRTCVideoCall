package com.example.firebasewebrtc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firebasewebrtc.ui.IncomingCallActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        Log.d("FCM", "New token: $token")

        val calleeId: String? = SharedPreferenceUtil.getFCMToken()
        if (!calleeId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance().collection(AppConstants.FCM_collection)
                .document(calleeId).update("fcmToken", token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        val localFcmData = FirebaseRMessage(
            callId = data["callId"],
            calleeId = data["calleeId"],
            title = data["title"],
            callType = data["callType"],
            callerNumber = data["callerNumber"],
            body = data["body"]
        )

        Log.e("FCM", "onMessageReceived: " + Gson().toJson(localFcmData))

        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("callId", localFcmData.callId)
            putExtra("callerId", localFcmData.calleeId)
            putExtra(AppConstants.Common_Transfer_Data, Gson().toJson(localFcmData).toString())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        this.startActivity(intent)

//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

//        val notification = NotificationCompat.Builder(this, "call_channel")
//            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(localFcmData.title)
//            .setContentText(localFcmData.body).setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setCategory(NotificationCompat.CATEGORY_CALL)
//            .setFullScreenIntent(pendingIntent, true) // ✅ KEY LINE
//            .build()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "call_channel", "Incoming Calls", NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Channel for incoming call notifications"
//                enableLights(true)
//                enableVibration(true)
//                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//            }
//
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }

//        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        manager.notify(1, notification)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "call_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Call Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Your call icon
            .setContentTitle(title).setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL).setAutoCancel(true)

        notificationManager.notify(1, notificationBuilder.build())
    }
}