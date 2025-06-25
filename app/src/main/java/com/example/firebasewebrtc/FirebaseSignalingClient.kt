package com.example.firebasewebrtc

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSignalingClient(
    callOrSessionId: String,
    private val listener: SignalingListener,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val callDoc = firestore.collection(AppConstants.FCM_collection).document(callOrSessionId)

    private var callListener: ListenerRegistration? = null
    private var iceCandidateListener: ListenerRegistration? = null

    init {
        // Listen for SDP offer/answer
        callListener = callDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Firestore", "Listener error", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: return@addSnapshotListener

                val type = data["type"] as? String /*as? String ?: return@addSnapshotListener*/
                val sdp = data["sdp"] as? String /*as? String ?: return@addSnapshotListener*/
                Log.e("Firestore", "Snapshot data: ${type} " + Gson().toJson(snapshot.data))


                if (!type.isNullOrEmpty() && !sdp.isNullOrEmpty()) {
                    val session = SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type), sdp
                    )
                    listener.onRemoteSessionReceived(session)
                }

//                if (!type.isNullOrEmpty() && type == "end") {
//                    listener.onCallEnded()
//                }
            }
        }

        // Listen for ICE candidates
        iceCandidateListener =
            callDoc.collection("candidates").addSnapshotListener { snapshots, _ ->
                Log.e("Firestore", "iceCandidateListener: ${snapshots}")

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
        Log.e("Firestore", "Sending offer SDP: ${offer.description}")

        val data = mutableMapOf<String, Any>(
            "type" to "offer", "sdp" to offer.description
        )

        tergateBUserCallId?.let { data["calleeId"] = it }
        mCurrentUserCallId?.let { data["callerId"] = it }

        callDoc.set(data, SetOptions.merge()).addOnSuccessListener {
            Log.d("Firestore", "Offer successfully sent.")
        }.addOnFailureListener {
            Log.e("Firestore", "Failed to send offer: ${it.message}")
        }
    }

    fun sendAnswer(answer: SessionDescription) {
        Log.e("Firestore", "sendAnswer: ${answer.type}")
        callDoc.set(mapOf("type" to "answer", "sdp" to answer.description), SetOptions.merge())
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
//        firestore.collection("calls").document(callOrSessionId)
        callDoc.set(mapOf("type" to "end", "sdp" to null), SetOptions.merge())
    }

    fun release() {
        callListener?.remove()
        iceCandidateListener?.remove()
    }
}