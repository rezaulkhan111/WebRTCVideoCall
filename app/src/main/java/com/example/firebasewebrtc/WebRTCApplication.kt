package com.example.firebasewebrtc

import android.app.Application
import android.content.SharedPreferences
import com.example.firebasewebrtc.di.ApplicationComponent
import com.example.firebasewebrtc.di.DaggerApplicationComponent
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import org.webrtc.PeerConnectionFactory
import javax.inject.Inject

@HiltAndroidApp
class WebRTCApplication : Application() {

    val applicationComponent: ApplicationComponent by lazy {
        initializeComponent()
    }

    private fun initializeComponent(): ApplicationComponent {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
//        instance = this
//        preferences = getSharedPreferences("com.example.firebasewebrtc", MODE_PRIVATE)
    }

    companion object {
//        lateinit var instance: WebRTCApplication
//            private set

//        @Inject
//        lateinit var preferences: SharedPreferences
//            private set

//        fun getAppContext(): WebRTCApplication {
//            return instance
//        }
    }
}