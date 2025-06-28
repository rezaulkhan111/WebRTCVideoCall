package com.example.firebasewebrtc.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebasewebrtc.FirebaseSignalingClient
import com.example.firebasewebrtc.IApiService
import com.example.firebasewebrtc.NotificationRequest
import com.example.firebasewebrtc.RetrofitClientInstance
import com.example.firebasewebrtc.SharedPreferenceUtil
import com.example.firebasewebrtc.SignalingListener
import com.example.firebasewebrtc.data.WebRtcEventListener
import com.example.firebasewebrtc.data.WebRtcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallingVM(appRef: Application) : AndroidViewModel(appRef), SignalingListener,
    WebRtcEventListener {

    private val _callStatus = MutableStateFlow("Initializing")
    val callStatus: StateFlow<String> = _callStatus
    lateinit var webRtcManager: WebRtcManager
    private var signalingClient: FirebaseSignalingClient? = null

    fun initCall(sessionId: String) {
        webRtcManager = WebRtcManager(
            getApplication(),
            eventListener = this
        )
        webRtcManager.initPeerConnectionFactory()
        webRtcManager.createPeerConnection()

        signalingClient = FirebaseSignalingClient(
            callOrSessionId = sessionId, listener = this
        )

//        webRtcManager.createOffer { offer ->
//            signalingClient?.sendOffer(
//                offer,
//                tergateBUserCallId = sessionId,
//                mCurrentUserCallId = SharedPreferenceUtil.getFCMCallerId()
//            )
//        }
        fetchNotification(sessionId)
    }

    fun initLocalRenderer(surfaceRenderer: SurfaceViewRenderer) {
        webRtcManager.initLocalStream(surfaceRenderer)
    }

    fun setRemoteRenderer(surfaceRenderer: SurfaceViewRenderer) {
        surfaceRenderer.init(webRtcManager.eglBaseRef.eglBaseContext, null)
        surfaceRenderer.setZOrderMediaOverlay(true)
    }

    override fun onRemoteSessionReceived(session: SessionDescription) {
        webRtcManager.setRemoteDescription(session)
        if (session.type == SessionDescription.Type.OFFER) {
            webRtcManager.createAnswer { answer ->
                signalingClient?.sendAnswer(answer)
            }
        }
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        webRtcManager.addIceCandidate(iceCandidate)
    }

    override fun onCallEnded() {
        _callStatus.value = "Call Ended"
        webRtcManager.close()
        signalingClient?.release()
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        signalingClient?.sendIceCandidate(candidate)
    }

    override fun onAddRemoteStream(stream: MediaStream) {
        viewModelScope.launch {
            _callStatus.emit("Remote stream received")
        }
    }

    override fun onConnectionEstablished() {
        _callStatus.value = "Call Connected"
    }

    fun endCall() {
        signalingClient?.sendCallEnded()
        onCallEnded()
    }

    fun startCallTimeout(durationMs: Long = 30000, onTimeout: () -> Unit) {
        viewModelScope.launch {
            delay(durationMs)
            onTimeout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        webRtcManager.close()
        signalingClient?.release()
    }

    fun cleanupCallSession() {
        signalingClient?.sendCallEnded()
        signalingClient?.release()
        webRtcManager.close()
    }


    fun fetchNotification(
        callOrSessionId: String
    ) {
        val dateService =
            RetrofitClientInstance.getRetrofitInstance()?.create(IApiService::class.java)
        val call = dateService?.requestNotification(
            NotificationRequest(
                calleeId = callOrSessionId,
                title = "📞 Incoming Call",
                body = "User ${SharedPreferenceUtil.getFCMCallerId()} is calling you...",
                callId = callOrSessionId
            )
        )
        call!!.enqueue(object : Callback<NotificationRequest?> {
            @SuppressLint("NewApi", "SetTextI18n")
            override fun onResponse(
                call: Call<NotificationRequest?>, response: Response<NotificationRequest?>
            ) {
                if (response.isSuccessful) {
                    webRtcManager.createOffer { offer ->
                        signalingClient?.sendOffer(
                            offer,
                            tergateBUserCallId = callOrSessionId,
                            mCurrentUserCallId = SharedPreferenceUtil.getFCMCallerId()
                        )
                    }
                } else {
                    Log.e("CallActivity", "else")
                }
            }

            override fun onFailure(call: Call<NotificationRequest?>, t: Throwable) {
                Log.e("CallActivity", "else: " + t.message)
            }
        })
    }
}