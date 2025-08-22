package com.example.firebasewebrtc.di

import android.content.Context
import com.example.firebasewebrtc.di.module.AppModule
import com.example.firebasewebrtc.di.module.NetworkModule
import com.example.firebasewebrtc.di.module.RepositoryModule
import com.example.firebasewebrtc.di.module.WebRtcModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [NetworkModule::class, AppModule::class, RepositoryModule::class, WebRtcModule::class]
)
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}