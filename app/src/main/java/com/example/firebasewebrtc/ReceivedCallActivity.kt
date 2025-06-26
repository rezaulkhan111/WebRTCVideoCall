package com.example.firebasewebrtc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

class ReceivedCallActivity : BaseActivity(), PeerConnection.Observer {
    private lateinit var localView: SurfaceViewRenderer
    private lateinit var remoteView: SurfaceViewRenderer
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection

    private lateinit var localVideoTrack: VideoTrack
    private lateinit var remoteVideoTrack: VideoTrack

    private lateinit var videoCapturer: VideoCapturer
    private lateinit var eglBase: EglBase
    private lateinit var mSignalingClient: FirebaseSignalingClient

    private var callOrSessionId: String = ""
    private var localIsCaller = false

    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    )
    private val PERMISSION_REQUEST_CODE = 1

    val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received_call)

        localView = findViewById<SurfaceViewRenderer>(R.id.localView)
        remoteView = findViewById<SurfaceViewRenderer>(R.id.remoteView)

        callOrSessionId = intent.getStringExtra("callId").toString()
        localIsCaller = intent.getBooleanExtra("isCaller", true)

        if (!callOrSessionId.isNullOrEmpty()) {
            Log.e("RCallActivity", "callOrSessionId: " + callOrSessionId)
            requestPermissionsIfNeeded()
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this, PERMISSIONS, PERMISSION_REQUEST_CODE
            )
        } else {
            startCallSetup()
        }
    }

    private fun hasPermissions(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCallSetup()
        } else {
            Toast.makeText(this, "Camera & Audio permission required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCallSetup() {
        val endCallButton = findViewById<Button>(R.id.btnEndCall)

        eglBase = EglBase.create()
        localView.init(eglBase.eglBaseContext, null)
        remoteView.init(eglBase.eglBaseContext, null)
        localView.setZOrderMediaOverlay(true)

        val options =
            PeerConnectionFactory.InitializationOptions.builder(this).setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-IntelVP8/Enabled/").createInitializationOptions()

        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .createPeerConnectionFactory()

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource).apply {
            setEnabled(true)
        }


        mSignalingClient = FirebaseSignalingClient(callOrSessionId, object : SignalingListener {

            override fun onRemoteSessionReceived(session: SessionDescription) {
                peerConnection.setRemoteDescription(SimpleSdpObserver(), session)

                if (session.type == SessionDescription.Type.OFFER) {
                    peerConnection.createAnswer(object : SdpObserver {

                        override fun onCreateSuccess(answer: SessionDescription) {
                            peerConnection.setLocalDescription(SimpleSdpObserver(), answer)
                            mSignalingClient.sendAnswer(answer)
                        }

                        override fun onSetSuccess() {}
                        override fun onCreateFailure(error: String) {
                            Log.e("ReceivedCall", "Answer creation failed: $error")
                        }

                        override fun onSetFailure(error: String) {
                            Log.e("ReceivedCall", "Set local description failed: $error")
                        }
                    }, MediaConstraints())
                }
            }

            override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                peerConnection.addIceCandidate(iceCandidate)
            }

            override fun onCallEnded() {
                mSignalingClient.release()
                releaseAllConn()
            }
        })

        peerConnection = createPeerConnection(
            peerConnectionFactory, signalingClient = mSignalingClient, remoteView
        )

        val localMedia = addLocalMedia(
            peerConnectionFactory, peerConnection, this, eglBase.eglBaseContext
        )

        localVideoTrack = localMedia.videoTrack
        videoCapturer = localMedia.videoCapturer

        peerConnection.addTrack(localVideoTrack, listOf("ARDAMS"))
        peerConnection.addTrack(localAudioTrack, listOf("ARDAMS"))

        localVideoTrack.addSink(localView)

        // 5. End Call Button
        endCallButton.setOnClickListener {
            mSignalingClient.sendCallEnded()
            releaseAllConn()
        }
    }

    private fun releaseAllConn() {
        try {
            peerConnection.close()
            videoCapturer.stopCapture()
            localView.release()
            remoteView.release()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {

    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
        Log.d("ICE", "State: $state")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        candidate?.let { mSignalingClient.sendIceCandidate(it) }
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
    }

    override fun onAddStream(stream: MediaStream?) {
//        if (stream != null && stream.videoTracks.isNullOrEmpty()) {
//            remoteVideoTrack = stream.videoTracks[0]
//            remoteVideoTrack.addSink(remoteView)
//        }
    }

    override fun onRemoveStream(p0: MediaStream?) {

    }

    override fun onDataChannel(p0: DataChannel?) {
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        val trackRece = transceiver?.receiver
        if (trackRece != null) {
            val mTrackType = trackRece.track()

            if (mTrackType is VideoTrack) {
                remoteVideoTrack = mTrackType
                remoteVideoTrack.addSink(remoteView)
            }
            if (mTrackType is AudioTrack) {
                mTrackType.setEnabled(true)
            }
//            else if (mTrackType is AudioTrack) {
//                remoteVideoTrack = mTrackType
//
//                remoteVideoTrack?.setEnabled(true)
//            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        peerConnection.close()
        videoCapturer.stopCapture()
        localView.release()
        remoteView.release()
        peerConnectionFactory.dispose()
    }
}