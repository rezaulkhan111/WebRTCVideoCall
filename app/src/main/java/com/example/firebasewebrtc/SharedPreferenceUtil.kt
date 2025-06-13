package com.example.firebasewebrtc

import android.util.Log
import androidx.core.content.edit

object SharedPreferenceUtil {
    private var preferences = WebRTCApplication.preferences

    fun getFCMToken() = preferences.getString(
        SharedPreferenceConstants.FCM_TOKEN_KEY,
        ""
    )

    fun setFCMToken(token: String?) {
        preferences.edit {
            putString(SharedPreferenceConstants.FCM_TOKEN_KEY, "$token")
        }
    }

    fun getFCMCallerId() = preferences.getString(
        SharedPreferenceConstants.FCM_CALL_ID_KEY,
        ""
    )

    fun setFCMCallerId(callId: String?) {
        Log.e("SD", "setFCMCallId: " + callId)
        preferences.edit {
            putString(SharedPreferenceConstants.FCM_CALL_ID_KEY, "$callId")
        }
    }
}

object SharedPreferenceConstants {
    const val FCM_TOKEN_KEY = "auth_token"
    const val FCM_CALL_ID_KEY = "call_id_data"
}