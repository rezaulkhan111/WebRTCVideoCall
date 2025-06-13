package com.example.firebasewebrtc

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class WebRTCApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = getSharedPreferences("com.example.firebasewebrtc", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var instance: WebRTCApplication
            private set

        lateinit var preferences: SharedPreferences
            private set

        fun getAppContext(): WebRTCApplication {
            return instance
        }
    }
}