package com.example.firebasewebrtc.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.firebasewebrtc.utils.AppConstants
import com.example.firebasewebrtc.presentation.base.BaseActivity
import com.example.firebasewebrtc.databinding.ActivityReceivedCallBinding
import com.example.firebasewebrtc.presentation.viewmodel.CallingVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

@AndroidEntryPoint
class ReceivedCallActivity : BaseActivity() {
    private lateinit var binding: ActivityReceivedCallBinding
    private val callViewModel: CallingVM by viewModels()
    private lateinit var sessionId: String
    private var hasEnded = false
    private var mAudioVideoCallStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceivedCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra("callId").toString()
        mAudioVideoCallStatus = intent.getBooleanExtra(AppConstants.isAudioOrVideo, false)

        if (sessionId.isNotEmpty()) {
            setupVideoViews(
                binding.localView, binding.remoteView
            )

            requestPermissionsIfNeeded {
                callViewModel.initCallSend(
                    sessionId, binding.localView, false, mAudioVideoCallStatus
                )

                if (mAudioVideoCallStatus) {
                    callViewModel.setRemoteRenderer(binding.remoteView)
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
                    binding.callStatusText.text = status

                    when (status) {
                        "Call Ended" -> {
//                            if (!hasEnded) {
//                                hasEnded = true
////                                Handler(Looper.getMainLooper()).postDelayed({
////                                    finish()
////                                }, 1000) // wait 1 second before closing
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