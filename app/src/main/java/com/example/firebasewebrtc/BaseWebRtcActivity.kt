package com.example.firebasewebrtc

import android.util.Log
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

abstract class BaseWebRtcActivity : BaseActivity(),
    PeerConnection.Observer, SignalingListener {

    protected lateinit var peerConnectionFactory: PeerConnectionFactory
    protected var peerConnection: PeerConnection? = null
    protected var localMedia: LocalMedia? = null
    protected lateinit var eglBase: EglBase
    protected lateinit var signalingClient: FirebaseSignalingClient


    protected fun initWebRtcAndCall() {
        try {
//            initEglBaseAndRenderers()
//            initPeerConnectionFactory()
//            setupSignalingClient() // This activity implements SignalingListener
//            createPeerConnectionInstance()
//            setupLocalMediaTracks()
//            addTracksToPeerConnection()
            // Child activities will implement specific call initiation (offer/answer)
            onWebRtcReady()
        } catch (e: Exception) {
            Log.e("BaseWebRtc", "Error setting up WebRTC: ${e.message}", e)
            releaseWebRtcResources()
            finish()
        }
    }

    protected abstract fun onWebRtcReady()


    protected fun releaseWebRtcResources() {

    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWebRtcResources()
    }
}