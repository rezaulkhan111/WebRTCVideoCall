package com.example.firebasewebrtc

import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

fun answerCall(peerConnection: PeerConnection, offerSdp: SessionDescription) {
    val constraints = MediaConstraints()
    peerConnection.setRemoteDescription(SimpleSdpObserver(), offerSdp)
    peerConnection.createAnswer(object : SimpleSdpObserver() {
        override fun onCreateSuccess(sdp: SessionDescription?) {
            peerConnection.setLocalDescription(this, sdp)
            // Now send answer via FirebaseSignalingClient
        }
    }, constraints)
}