package com.example.firebasewebrtc

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

fun createPeerConnection(
    factory: PeerConnectionFactory,
    signalingClient: FirebaseSignalingClient,
    remoteView: SurfaceViewRenderer
): PeerConnection {

    val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )
    val config = PeerConnection.RTCConfiguration(iceServers)
    config.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

    return factory.createPeerConnection(config, object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            signalingClient.sendIceCandidate(candidate)
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
        }

        override fun onAddStream(stream: MediaStream) {
            // Handle remote stream
        }

        // Implement other required callbacks with empty bodies
        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
        override fun onDataChannel(dc: DataChannel) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState) {}
        override fun onRemoveStream(p0: MediaStream) {}
        override fun onSignalingChange(p0: PeerConnection.SignalingState) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver, p1: Array<out MediaStream>) {
            val track = receiver.track()
            if (track is VideoTrack) {
                track.addSink(remoteView)
            }
        }
    }) ?: throw IllegalStateException("Failed to create PeerConnection")
}