package com.example.firebasewebrtc.service

import android.content.Intent
import android.util.Log
import com.example.firebasewebrtc.data.model.FirebaseRMessageDTO
import com.example.firebasewebrtc.data.pref.SharedPreferenceUtil
import com.example.firebasewebrtc.presentation.ui.IncomingCallActivity
import com.example.firebasewebrtc.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPref: SharedPreferenceUtil

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val calleeId: String? = sharedPref.getFCMToken()
        if (!calleeId.isNullOrEmpty()) {
            FirebaseFirestore
                .getInstance()
                .collection(AppConstants.FCM_collection)
                .document(calleeId)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        val localFcmData = FirebaseRMessageDTO(
            callId = data["callId"],
            calleeId = data["calleeId"],
            title = data["title"],
            callType = data["callType"],
            callerNumber = data["callerNumber"],
            body = data["body"]
        )

//        val intent = Intent(this, IncomingCallActivity::class.java).apply {
//            putExtra("callId", localFcmData.callId)
//            putExtra("callerId", localFcmData.calleeId)
//            putExtra(AppConstants.Common_Transfer_Data, Gson().toJson(localFcmData).toString())
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        this.startActivity(intent)

        val intent = Intent(this, CallService::class.java).apply {
            putExtra(AppConstants.Common_Transfer_Data, Gson().toJson(localFcmData))
            action = AppConstants.ACTION_START_CALL
        }

        startService(intent)
    }
}