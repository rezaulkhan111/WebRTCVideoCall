package com.example.firebasewebrtc.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebasewebrtc.data.model.NotificationRequestDTO
import com.example.firebasewebrtc.data.pref.SharedPreferenceUtil
import com.example.firebasewebrtc.data.signaling.FirebaseSignalingClient
import com.example.firebasewebrtc.domain.repository.ICallRepository
import com.example.firebasewebrtc.domain.signaling.SignalingListener
import com.example.firebasewebrtc.presentation.webrtc.WebRtcEventListener
import com.example.firebasewebrtc.presentation.webrtc.WebRtcManager
import com.example.firebasewebrtc.utils.ApiResult
import com.google.gson.Gson
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@HiltViewModel
class CallingVM @Inject constructor(
    private val repository: ICallRepository,
    private val sharedPref: SharedPreferenceUtil,
    @ApplicationContext private val context: Context
) : ViewModel(), SignalingListener, WebRtcEventListener {

    private val _callStatus = MutableStateFlow("Initializing")
    private val _repositories = MutableStateFlow<NotificationRequestDTO?>(null)
    private val _canFetchMessage = MutableStateFlow<String?>(null)


    val callStatus: StateFlow<String> = _callStatus
    private var _webRtcManager: WebRtcManager? = null
    private var _signalingClient: FirebaseSignalingClient? = null

    private val _fcmCallerId = MutableStateFlow<String?>(null)
    val fcmCallerId: StateFlow<String?> = _fcmCallerId

    init {
        // Read the data when the ViewModel is created
        _fcmCallerId.value = sharedPref.getFCMCallerId()
    }

    fun initCallSend(
        sessionId: String,
        svrLocalView: SurfaceViewRenderer,
        isCaller: Boolean = false,
        isAudioOnly: Boolean = false
    ) {
        if (_webRtcManager != null) {
            Log.w("CallingVM", "Call already initialized. Ignoring...")
            return
        }

        _webRtcManager = WebRtcManager(
            contextRef = getApplication(context),
            eventListener = this,
            isAudioCallOnly = isAudioOnly
        )
        _webRtcManager?.initPeerConnectionFactory()
        _webRtcManager?.initLocalStream(svrLocalView)
        _webRtcManager?.createPeerConnection()

        _signalingClient = FirebaseSignalingClient(
            callOrSessionId = sessionId, listener = this
        )

        if (isCaller) {
            requestNotification(sessionId, isAudioOnly)
        }
    }

    fun setRemoteRenderer(svrRemoteRenderer: SurfaceViewRenderer) {
//        if (_webRtcManager?.isInitialized) {
        _webRtcManager?.setRemoteView(svrRemoteRenderer)
//        } else {
//            Log.w("CallingVM", "WebRtcManager not initialized yet!")
//        }
    }

    override fun onRemoteSessionReceived(session: SessionDescription) {
        Log.d("CALLING_VM", "Remote session received: ${session.type}")
        _webRtcManager?.setRemoteDescription(session)
        if (session.type == SessionDescription.Type.OFFER) {
            _callStatus.value = "Incoming Call"
            _webRtcManager?.createAnswer { answer ->
                _signalingClient?.sendAnswer(answer)
                _callStatus.value = "Answered"
            }
        }
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        _webRtcManager?.addIceCandidate(iceCandidate)
    }

    override fun onCallEnded() {
        _callStatus.value = "Call Ended"
//        _webRtcManager?.close()
//        _signalingClient?.release()
        cleanupCallSession()
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        _signalingClient?.sendIceCandidate(candidate)
    }

    override fun onAddRemoteStream(stream: MediaStream) {
//        viewModelScope.launch {
        _callStatus.value = "Remote stream received"
//        }
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
            if (_callStatus.value != "Call Connected" /*&& _callStatus.value != "Answered"*/) {
//                _callStatus.value = "Call Ended"
                onTimeout()
                onCallEnded()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupCallSession()
//        try {
//            _webRtcManager?.close()
//            _signalingClient?.release()
//        } catch (e: Exception) {
//            Log.e("CallingVM", "Cleanup failed: ${e.message}")
//        }
    }

    fun cleanupCallSession() {
        try {
//            _signalingClient?.sendCallEnded()
            _signalingClient?.release()
            _webRtcManager?.close()
        } catch (e: Exception) {
            Log.e("CallingVM", "Cleanup failed: ${e.message}")
        } finally {
            _webRtcManager = null
            _signalingClient = null
        }
    }

    private fun requestNotification(
        callOrSessionId: String, isAudioOnly: Boolean = false
    ) {
        viewModelScope.launch {
            when (val result = repository.requestNotification(
                NotificationRequestDTO(
                    calleeId = callOrSessionId,
                    title = "📞 Incoming Call",
                    body = "User ${sharedPref.getFCMCallerId()} is calling you...",
                    callId = callOrSessionId,
                    callType = isAudioOnly.toString()
                )
            )) {
                is ApiResult.Success -> {
                    _repositories.value = result.data

                    _webRtcManager?.createOffer { offer ->
                        Log.e("CALLING_VM", "Offer created, sending..." + Gson().toJson(offer))
                        _signalingClient?.sendOffer(
                            offer,
                            targetBUserCallId = callOrSessionId,
                            mCurrentUserCallId = sharedPref.getFCMCallerId()
                        )
                    }
                }

                is ApiResult.Error -> {
                    _canFetchMessage.value = result.exception.message
                }
            }
        }
    }
}