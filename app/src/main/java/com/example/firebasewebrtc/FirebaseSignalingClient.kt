package com.example.firebasewebrtc

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSignalingClient(
    val calleeId: String,
    private val listener: SignalingListener,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val callDoc = firestore.collection(AppConstants.FCM_collection).document(calleeId)

    private var callListener: ListenerRegistration? = null
    private var iceCandidateListener: ListenerRegistration? = null

    init {
        // Listen for SDP offer/answer
//        callListener = callDoc.addSnapshotListener { snapshot, _ ->
//            if (snapshot != null && snapshot.exists()) {
//                val data = snapshot.data ?: return@addSnapshotListener
//                val type = data["type"] as? String ?: return@addSnapshotListener
//                val sdp = data["sdp"] as? String ?: return@addSnapshotListener
//
//                val session = SessionDescription(
//                    SessionDescription.Type.fromCanonicalForm(type), sdp
//                )
//                listener.onRemoteSessionReceived(session)
//            }
//        }

        // Listen for ICE candidates
        iceCandidateListener =
            callDoc.collection("candidates").addSnapshotListener { snapshots, _ ->
                snapshots?.documentChanges?.forEach { change ->
                    val doc = change.document
                    val candidate = IceCandidate(
                        doc["sdpMid"] as String,
                        (doc["sdpMLineIndex"] as Long).toInt(),
                        doc["sdpCandidate"] as String
                    )
                    listener.onIceCandidateReceived(candidate)
                }
            }
    }

    fun sendOffer(
        offer: SessionDescription,
        tergateBUserCallId: String? = null,
        mCurrentUserCallId: String? = null
    ) {
        // 2. Send call notification via HTTP to local server
        callDoc.update(
            mapOf(
                "type" to "offer",
                "sdp" to offer.description,
                "calleeId" to tergateBUserCallId,
                "callerId" to mCurrentUserCallId
            )
        )
    }

    fun sendAnswer(answer: SessionDescription) {
        callDoc.update(
            mapOf(
                "type" to "answer", "sdp" to answer.description
            )
        )
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        callDoc.collection("candidates").add(
            mapOf(
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "sdpCandidate" to candidate.sdp
            )
        )
    }

    fun sendCallEnded() {
        firestore.collection("calls").document(calleeId).delete()
    }

    fun release() {
        callListener?.remove()
        iceCandidateListener?.remove()
    }
}