package com.example.firebasewebrtc.data

import android.content.Context
import android.util.Log
import com.example.firebasewebrtc.SimpleSdpObserver
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
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcManager(
    private val contextRef: Context, private val eventListener: WebRtcEventListener
) {
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null

    private lateinit var videoCapturer: VideoCapturer
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    val eglBaseRef: EglBase = EglBase.create()

    fun initPeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(contextRef)
                .setEnableInternalTracer(true).setFieldTrials("WebRTC-IntelVP8/Enabled/")
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseRef.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseRef.eglBaseContext, true, true
                )
            ).createPeerConnectionFactory()
    }

    fun initLocalStream(localView: SurfaceViewRenderer) {
        localView.init(eglBaseRef.eglBaseContext, null)
        localView.setZOrderMediaOverlay(true)

        videoCapturer = createCameraCapturer()
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(
            SurfaceTextureHelper.create("CaptureThread", eglBaseRef.eglBaseContext),
            contextRef,
            videoSource?.capturerObserver
        )
        videoCapturer.startCapture(1280, 720, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("LOCAL_VIDEO_TRACK", videoSource)
        localVideoTrack?.addSink(localView)

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
        localAudioTrack = peerConnectionFactory.createAudioTrack(
            "101", peerConnectionFactory.createAudioSource(constraints)
        )
    }

    fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection =
            peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
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
                    }
                }

                override fun onRemoveStream(p0: MediaStream?) {}
                override fun onRenegotiationNeeded() {}
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            })

        peerConnection?.apply {
            localAudioTrack?.let { addTrack(it, listOf("ARDAMS")) }
            localVideoTrack?.let { addTrack(it, listOf("ARDAMS")) }
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

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let {
                    peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                    callback(it)
                }
            }
        }, constraints)
    }

    fun createAnswer(callback: (SessionDescription) -> Unit) {
//        val constraints = MediaConstraints().apply {
//            mandatory.apply {
//                add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
//                add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
//            }
//            optional.apply {
//                add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
//                add(MediaConstraints.KeyValuePair("googTypingDetection", "true"))
//                add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
//            }
//        }

        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let {
                    peerConnection?.setLocalDescription(SimpleSdpObserver(), it)
                    callback(it)
                }
            }
        }, constraints)
    }

    fun setRemoteDescription(desc: SessionDescription) {
        peerConnection?.setRemoteDescription(SimpleSdpObserver(), desc)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun close() {
        try {
            peerConnection?.close()
            if (::videoCapturer.isInitialized) {
                try {
                    videoCapturer.stopCapture()
                } catch (e: Exception) {
                    Log.e("WebRtcManager", "Error stopping capture: ${e.message}")
                }
                videoCapturer.dispose()
            }
            videoSource?.dispose()
            audioSource?.dispose()
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
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