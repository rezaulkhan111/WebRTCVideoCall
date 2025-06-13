package com.example.firebasewebrtc

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.io.IOException
import javax.security.auth.callback.Callback

class FirebaseSignalingClient(
    val callId: String,
    private val listener: SignalingListener,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val callDoc = firestore.collection(AppConstants.FCM_collection).document(callId)

    private var callListener: ListenerRegistration? = null
    private var iceCandidateListener: ListenerRegistration? = null

    init {
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

    fun sendOffer(offer: SessionDescription, calleeId: String? = null, callerId: String? = null) {
        callDoc.set(
            mapOf(
                "type" to "offer",
                "sdp" to offer.description,
                "callerId" to callerId,
                "calleeId" to calleeId
            )
        )


        // 2. Send call notification via HTTP to local server
        val jsonBody = JSONObject().apply {
            put("calleeId", calleeId)
            put("title", "📞 Incoming Call")
            put("body", "User $callerId is calling you...")
            put("callId", callId)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.0.110:3000/send-call-notification") // Your local Node server
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM", "Notification sent: ${response.body?.string()}")
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Notification send failed: ${e.message}")
            }
        })
    }

    fun sendAnswer(answer: SessionDescription) {
        callDoc.set(
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
        firestore.collection("calls").document(callId).delete()
    }

    fun release() {
        callListener?.remove()
        iceCandidateListener?.remove()
    }
}