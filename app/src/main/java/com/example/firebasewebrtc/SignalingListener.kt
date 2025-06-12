package com.example.firebasewebrtc

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignalingListener {
    fun onRemoteSessionReceived(session: SessionDescription)
    fun onIceCandidateReceived(iceCandidate: IceCandidate)
    fun onCallEnded()
}