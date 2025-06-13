package com.example.firebasewebrtc

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
}

object SharedPreferenceConstants {
    const val FCM_TOKEN_KEY = "auth_token"
}