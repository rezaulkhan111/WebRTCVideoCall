package com.example.firebasewebrtc

object AppConstants {
    const val FCM_collection = "calls"

    const val FCM_COLLECTION = "webrtc_users" // Your Firebase collection name for users
    const val VIDEO_TRACK_ID = "LOCAL_VIDEO_TRACK"
    const val AUDIO_TRACK_ID = "LOCAL_AUDIO_TRACK"
    const val MEDIA_STREAM_ID = "main_stream" // A common ID for the MediaStream
    const val CALL_CHANNEL_ID = "call_channel"
    const val CALL_CHANNEL_NAME = "Incoming Calls"
    const val CALL_NOTIFICATION_ID = 1
    const val EXTRA_CALL_ID = "callId"
    const val EXTRA_IS_CALLER = "isCaller"
    const val EXTRA_CALLER_ID = "callerId"
    const val CAMERA_RESOLUTION_WIDTH = 1280
    const val CAMERA_RESOLUTION_HEIGHT = 720
    const val CAMERA_FPS = 30
}