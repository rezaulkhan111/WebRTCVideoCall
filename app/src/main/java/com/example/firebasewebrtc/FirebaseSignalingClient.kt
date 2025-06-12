package com.example.firebasewebrtc

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSignalingClient(
    val callId: String,
    private val listener: SignalingListener,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
//    private val callDoc = firestore.collection("calls").document(callId)
//    private val callerCandidates = callDoc.collection("callerCandidates")
//    private val calleeCandidates = callDoc.collection("calleeCandidates")

    private var callListener: ListenerRegistration? = null
    private var iceCandidateListener: ListenerRegistration? = null


    init {
        val callDoc = firestore.collection("calls").document(callId)
        // Listen for SDP offer/answer
        callListener = callDoc.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: return@addSnapshotListener
                val type = data["type"] as? String ?: return@addSnapshotListener
                val sdp = data["sdp"] as? String ?: return@addSnapshotListener

                val session = SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(type), sdp
                )

                listener.onRemoteSessionReceived(session)
            }
        }

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

    fun sendOffer(offer: SessionDescription) {
        firestore.collection("calls").document(callId)
            .set(mapOf(
                "type" to "offer",
                "sdp" to offer.description
            ))
    }

    fun sendAnswer(answer: SessionDescription) {
        firestore.collection("calls").document(callId)
            .set(
                mapOf(
                    "type" to "answer",
                    "sdp" to answer.description
                )
            )
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        firestore.collection("calls").document(callId)
            .collection("candidates").add(
                mapOf(
                    "sdpMid" to candidate.sdpMid,
                    "sdpMLineIndex" to candidate.sdpMLineIndex,
                    "sdpCandidate" to candidate.sdp
                )
            )
    }

    fun sendCallEnded() {
        firestore.collection("calls").document(callId).delete()
    }

    fun release() {
        callListener?.remove()
        iceCandidateListener?.remove()
    }
}