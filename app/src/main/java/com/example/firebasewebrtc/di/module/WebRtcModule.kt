package com.example.firebasewebrtc.di.module

import android.content.Context
import com.example.firebasewebrtc.presentation.webrtc.WebRtcEventListener
import com.example.firebasewebrtc.presentation.webrtc.WebRtcManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
class WebRtcModule {
    @Provides
    fun provideWebRtcManager(
        @ApplicationContext context: Context,
        eventListener: WebRtcEventListener,
        isAudioCallOnly: Boolean
    ): WebRtcManager {
        return WebRtcManager(
            contextRef = context,
            eventListener = eventListener,
            isAudioCallOnly = isAudioCallOnly
        )
    }
}