package com.example.firebasewebrtc

import android.content.Context
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

fun addLocalMedia(
    factory: PeerConnectionFactory,
    peerConnection: PeerConnection,
    context: Context,
    eglBaseContext: EglBase.Context
): LocalMedia {
    var mVideoTrack: VideoTrack
    var mAudioTrack: AudioTrack

    val mLocalMedia: LocalMedia

    val mVideoCapturer = createCameraCapturer(Camera2Enumerator(context))!!
    val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)

//    val mStream = factory.createLocalMediaStream("localStream")

    val videoSource = factory.createVideoSource(mVideoCapturer.isScreencast)
    mVideoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
    mVideoCapturer.startCapture(1280, 720, 30)
    mVideoTrack = factory.createVideoTrack("LOCAL_VIDEO_TRACK", videoSource)
    mAudioTrack =
        factory.createAudioTrack("LOCAL_AUDIO_TRACK", factory.createAudioSource(MediaConstraints()))
//    mStream.addTrack(mVideoTrack)
//    mStream.addTrack(mAudioTrack)
//    peerConnection.addStream(mStream)

    return LocalMedia(
        videoTrack = mVideoTrack,
        audioTrack = mAudioTrack,
        videoCapturer = mVideoCapturer,
/*        stream = null*/
    )
}

data class LocalMedia(
    val videoTrack: VideoTrack,
    val audioTrack: AudioTrack,
    val videoCapturer: VideoCapturer,
/*    val stream: MediaStream*/
)