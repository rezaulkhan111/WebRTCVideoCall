package com.example.firebasewebrtc.utils

object AppConstants {
    const val FCM_collection = "calls"
    const val isAudioOrVideo = "AudioVideoCallPref"
    const val Common_Transfer_Data = "JsonData"
}

enum class VideoService {
    LOCAL_VIDEO_TRACK,
    LOCAL_AUDIO_TRACK
}