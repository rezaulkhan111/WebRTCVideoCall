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
import com.google.gson.Gson
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

    private lateinit var _webRtcManager: WebRtcManager
    private var _signalingClient: FirebaseSignalingClient? = null

    fun initCallSend(
        sessionId: String,
        svrLocalView: SurfaceViewRenderer,
        isCaller: Boolean = false,
        isAudioOnly: Boolean = false
    ) {
        _webRtcManager =
            WebRtcManager(getApplication(), eventListener = this, isAudioCallOnly = isAudioOnly)
        _webRtcManager.initPeerConnectionFactory()
        _webRtcManager.initLocalStream(svrLocalView)
        _webRtcManager.createPeerConnection()

        _signalingClient = FirebaseSignalingClient(
            callOrSessionId = sessionId, listener = this
        )

        if (isCaller) {
            fetchNotification(sessionId, isAudioOnly)
        }
    }

    fun setRemoteRenderer(svrRemoteRenderer: SurfaceViewRenderer) {
        if (::_webRtcManager.isInitialized) {
            _webRtcManager.setRemoteView(svrRemoteRenderer)
        } else {
            Log.w("CallingVM", "WebRtcManager not initialized yet!")
        }
    }

    override fun onRemoteSessionReceived(session: SessionDescription) {
        Log.d("CALLING_VM", "Remote session received: ${session.type}")
        _webRtcManager.setRemoteDescription(session)
        if (session.type == SessionDescription.Type.OFFER) {
            _callStatus.value = "Incoming Call"
            _webRtcManager.createAnswer { answer ->
                _signalingClient?.sendAnswer(answer)
                _callStatus.value = "Answered"
            }
        }
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        _webRtcManager.addIceCandidate(iceCandidate)
    }

    override fun onCallEnded() {
        _callStatus.value = "Call Ended"
        _webRtcManager.close()
        _signalingClient?.release()
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        _signalingClient?.sendIceCandidate(candidate)
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
        _signalingClient?.sendCallEnded()
        onCallEnded()
    }

    fun startCallTimeout(durationMs: Long = 30000, onTimeout: () -> Unit) {
        viewModelScope.launch {
            delay(durationMs)

            // Only timeout if call hasn't connected
            if (_callStatus.value != "Call Connected" && _callStatus.value != "Answered") {
                _callStatus.value = "Call Ended"
                onTimeout()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            _webRtcManager.close()
            _signalingClient?.release()
        } catch (e: Exception) {
            Log.e("CallingVM", "Cleanup failed: ${e.message}")
        }
    }

    fun cleanupCallSession() {
        _signalingClient?.sendCallEnded()
        _signalingClient?.release()
        _webRtcManager.close()
    }

    private fun fetchNotification(
        callOrSessionId: String, isAudioOnly: Boolean = false
    ) {
        val dateService =
            RetrofitClientInstance.getRetrofitInstance()?.create(IApiService::class.java)
        val callService = dateService?.requestNotification(
            NotificationRequest(
                calleeId = callOrSessionId,
                title = "📞 Incoming Call",
                body = "User ${SharedPreferenceUtil.getFCMCallerId()} is calling you...",
                callId = callOrSessionId,
                callType = isAudioOnly.toString()
            )
        )
        callService!!.enqueue(object : Callback<NotificationRequest?> {
            @SuppressLint("NewApi", "SetTextI18n")
            override fun onResponse(
                call: Call<NotificationRequest?>, response: Response<NotificationRequest?>
            ) {
                if (response.isSuccessful) {
                    _webRtcManager.createOffer { offer ->
                        Log.e("CALLING_VM", "Offer created, sending..." + Gson().toJson(offer))
                        _signalingClient?.sendOffer(
                            offer,
                            targetBUserCallId = callOrSessionId,
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