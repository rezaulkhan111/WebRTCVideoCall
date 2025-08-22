package com.example.firebasewebrtc.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.firebasewebrtc.utils.AppConstants
import com.example.firebasewebrtc.presentation.base.BaseActivity
import com.example.firebasewebrtc.databinding.ActivityCallBinding
import com.example.firebasewebrtc.presentation.viewmodel.CallingVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

@AndroidEntryPoint
class CallActivity : BaseActivity() {
    private lateinit var binding: ActivityCallBinding
    private val callViewModel: CallingVM by viewModels()
    private lateinit var sessionId: String

    private var mAudioVideoCallStatus: Boolean = false
    private var hasEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra("callId").toString()
        mAudioVideoCallStatus = intent.getBooleanExtra(AppConstants.isAudioOrVideo, false)

        if (sessionId.isNotEmpty()) {
            setupVideoViews(
                binding.localView, binding.remoteView
            )

            requestPermissionsIfNeeded {
                callViewModel.initCallSend(
                    sessionId, binding.localView, true, mAudioVideoCallStatus
                )

                if (mAudioVideoCallStatus) {
                    callViewModel.setRemoteRenderer(binding.remoteView)
                }

                callViewModel.startCallTimeout {
                    if (!hasEnded) {
//                        runOnUiThread {
//                            Toast.makeText(this, "Call timed out", Toast.LENGTH_SHORT).show()
//                            callViewModel.endCall()
//                            finish()
//                        }
                    }
                }
            }

            observeCallStatus()
        }

        binding.btnEndCall.setOnClickListener {
            callViewModel.endCall()
            this.finish()
        }
    }

    private fun setupVideoViews(
        localView: SurfaceViewRenderer, remoteView: SurfaceViewRenderer
    ) {
        localView.setZOrderMediaOverlay(true)
        localView.setMirror(true)
        remoteView.setMirror(false)
    }

    private fun observeCallStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                callViewModel.callStatus.collectLatest { status ->
                    Log.e("CALL_ACTIVITY", "Call status: $status")

                    binding.callStatusText.text = status

                    when (status) {
                        "Call Ended" -> {
//                            if (!hasEnded) {
//                                hasEnded = true
//                                Handler(Looper.getMainLooper()).postDelayed({
//                                    finish()
//                                }, 15000) // wait 1 second before closing
//                            }

                            finish()
                        }

                        "Initializing" -> {
                            // Optional: show loading
                        }

                        "Incoming Call", "Answered", "Call Connected" -> {
                            // Keep the screen active
                        }

                        else -> {
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callViewModel.cleanupCallSession()
    }
}