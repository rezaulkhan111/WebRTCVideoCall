package com.example.firebasewebrtc.data.pref

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.firebasewebrtc.WebRTCApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceUtil @Inject constructor(
    private val preferences: SharedPreferences
) {

    fun getFCMToken() = preferences.getString(
        SharedPreferenceConstants.FCM_TOKEN_KEY, ""
    )

    fun setFCMToken(token: String?) {
        preferences.edit {
            putString(SharedPreferenceConstants.FCM_TOKEN_KEY, "$token")
        }
    }

    fun getFCMCallerId() = preferences.getString(
        SharedPreferenceConstants.FCM_CALL_ID_KEY, ""
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