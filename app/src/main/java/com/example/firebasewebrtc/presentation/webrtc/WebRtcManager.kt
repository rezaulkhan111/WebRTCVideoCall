package com.example.firebasewebrtc.presentation.webrtc

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.firebasewebrtc.utils.VideoService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcManager @AssistedInject constructor(
    @Assisted private val contextRef: Context,
    private val eventListener: WebRtcEventListener,
    @Assisted private val isAudioCallOnly: Boolean = false
) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted contextRef: Context,
            @Assisted isAudioCallOnly: Boolean
        ): WebRtcManager
    }

    private lateinit var mPeerConnectionFactory: PeerConnectionFactory
    private var mPeerConnection: PeerConnection? = null

    private var mRemoteView: SurfaceViewRenderer? = null
    private var mLocalView: SurfaceViewRenderer? = null
    private lateinit var mVideoCapturer: VideoCapturer

    private var mVideoSource: VideoSource? = null
    private var mLocalVideoTrack: VideoTrack? = null
    private var mAudioSource: AudioSource? = null
    private var mLocalAudioTrack: AudioTrack? = null
    private var mEglBaseRef: EglBase = EglBase.create()

    fun initPeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(contextRef)
                .setEnableInternalTracer(true).setFieldTrials("WebRTC-IntelVP8/Enabled/")
                .createInitializationOptions()
        )

        mPeerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(mEglBaseRef.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    mEglBaseRef.eglBaseContext, true, true
                )
            ).createPeerConnectionFactory()
    }

    fun initLocalStream(localView: SurfaceViewRenderer) {
        if (isAudioCallOnly) {
            mLocalView = localView

            localView.init(mEglBaseRef.eglBaseContext, null)
            localView.setZOrderMediaOverlay(true)

            val audioManager = contextRef.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.apply {
                mode = AudioManager.MODE_IN_COMMUNICATION
                isSpeakerphoneOn = true // or false for earpiece
                isMicrophoneMute = false
            }

            mVideoCapturer = createCameraCapturer()
            mVideoSource = mPeerConnectionFactory.createVideoSource(mVideoCapturer.isScreencast)
            mVideoCapturer.initialize(
                SurfaceTextureHelper.create("CaptureThread", mEglBaseRef.eglBaseContext),
                contextRef,
                mVideoSource?.capturerObserver
            )
            mVideoCapturer.startCapture(1280, 720, 30)

            mLocalVideoTrack = mPeerConnectionFactory.createVideoTrack(
                VideoService.LOCAL_AUDIO_TRACK.name, mVideoSource
            )
            mLocalVideoTrack?.addSink(localView)
        }

        val audioConstraints = MediaConstraints().apply {
            mandatory.apply {
                add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            }
            optional.apply {
                add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                add(MediaConstraints.KeyValuePair("googTypingDetection", "true"))
                add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            }
        }
        mLocalAudioTrack = mPeerConnectionFactory.createAudioTrack(
            "101", mPeerConnectionFactory.createAudioSource(audioConstraints)
        )
    }

    fun setRemoteView(view: SurfaceViewRenderer) {
        mRemoteView = view
        mRemoteView?.init(mEglBaseRef.eglBaseContext, null)
        mRemoteView?.setZOrderMediaOverlay(true)
        mRemoteView?.setEnableHardwareScaler(true)
    }

    fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        mPeerConnection = mPeerConnectionFactory.createPeerConnection(
            rtcConfig, object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    eventListener.onIceCandidate(candidate)
                }

                override fun onAddStream(stream: MediaStream?) {
//                    stream?.let { eventListener.onAddRemoteStream(it) }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                        eventListener.onConnectionEstablished()
                    }
                }

                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
                override fun onDataChannel(p0: DataChannel?) {}
                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                override fun onAddTrack(receiver: RtpReceiver, p1: Array<out MediaStream>?) {
                    val track = receiver.track()
                    if (track is VideoTrack) {
//                        track.addSink(remoteView)
                        mRemoteView?.let {
                            track.addSink(it)
                        }
                    }
                }

                override fun onRemoveStream(p0: MediaStream?) {}
                override fun onRenegotiationNeeded() {}
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            })

        mPeerConnection?.apply {
            mLocalAudioTrack?.let { addTrack(it, listOf("ARDAMS")) }
            mLocalVideoTrack?.let { addTrack(it, listOf("ARDAMS")) }
        }
//        peerConnection?.addStream(stream)
    }

    fun createOffer(callback: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.apply {
                add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            }
            optional.apply {
                add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                add(MediaConstraints.KeyValuePair("googTypingDetection", "true"))
                add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            }
        }

        mPeerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let {
                    mPeerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                    callback(it)
                }
            }
        }, constraints)
    }

    fun createAnswer(callback: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints()
        mPeerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let {
                    mPeerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                    callback(it)
                }
            }
        }, constraints)
    }

    fun setRemoteDescription(desc: SessionDescription) {
        mPeerConnection?.setRemoteDescription(SimpleSdpObserver(), desc)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        mPeerConnection?.addIceCandidate(candidate)
    }

    fun close() {
        try {
            mPeerConnection?.close()
            mPeerConnection = null

            mLocalVideoTrack?.removeSink(mLocalView)
            mLocalVideoTrack?.dispose()
            mLocalVideoTrack = null

            mLocalAudioTrack?.dispose()
            mLocalAudioTrack = null

            mVideoCapturer.takeIf { ::mVideoCapturer.isInitialized }?.let {
                try {
                    it.stopCapture()
                } catch (e: Exception) {
                    Log.e("WebRtcManager", "Error stopping capture: ${e.message}")
                }
                it.dispose()
            }

            mVideoSource?.dispose()
            mVideoSource = null

            mAudioSource?.dispose()
            mAudioSource = null

            mLocalView?.release()
            mRemoteView?.release()
            mLocalView = null
            mRemoteView = null

            val audioManager = contextRef.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("WebRtcManager", "Error releasing resources: ${e.message}")
        }
    }

    private fun createCameraCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(contextRef)
        val deviceNames = enumerator.deviceNames

        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)!!
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)!!
            }
        }

        throw IllegalStateException("No camera found")
    }
}

interface WebRtcEventListener {
    fun onIceCandidate(candidate: IceCandidate)
    fun onAddRemoteStream(stream: MediaStream)
    fun onConnectionEstablished()
}