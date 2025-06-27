package com.example.firebasewebrtc

import android.content.Context
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

fun addLocalMedia(
    factory: PeerConnectionFactory,
    context: Context,
    eglBaseContext: EglBase.Context
): LocalMedia {
    var mVideoTrack: VideoTrack
    var mAudioTrack: AudioTrack

    val mVideoCapturer = createCameraCapturer(Camera2Enumerator(context))!!
    val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)


    val videoSource = factory.createVideoSource(mVideoCapturer.isScreencast)
    mVideoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
    mVideoCapturer.startCapture(1280, 720, 30)
    mVideoTrack = factory.createVideoTrack("LOCAL_VIDEO_TRACK", videoSource)
    mAudioTrack =
        factory.createAudioTrack("LOCAL_AUDIO_TRACK", factory.createAudioSource(MediaConstraints()))

    return LocalMedia(
        videoTrack = mVideoTrack,
        audioTrack = mAudioTrack,
        videoCapturer = mVideoCapturer
    )
}

data class LocalMedia(
    val videoTrack: VideoTrack,
    val audioTrack: AudioTrack,
    val videoCapturer: VideoCapturer
)