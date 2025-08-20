package com.example.firebasewebrtc.presentation.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(msg: String?) {}
    override fun onSetFailure(msg: String?) {}
}