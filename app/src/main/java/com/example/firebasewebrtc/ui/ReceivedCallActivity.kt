package com.example.firebasewebrtc.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.firebasewebrtc.BaseActivity
import com.example.firebasewebrtc.databinding.ActivityReceivedCallBinding
import com.example.firebasewebrtc.ui.viewmodel.CallingVM
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

class ReceivedCallActivity : BaseActivity() {
    private lateinit var binding: ActivityReceivedCallBinding
    private val callViewModel: CallingVM by viewModels()
    private lateinit var sessionId: String
    private var hasEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceivedCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra("callId").toString()

        if (sessionId.isNotEmpty()) {
            setupVideoViews(
                binding.localView,
                binding.remoteView
            )

            requestPermissionsIfNeeded {
                callViewModel.initCallSend(sessionId, binding.localView, false)
//                callViewModel.initLocalRenderer(binding.localView)
                callViewModel.setRemoteRenderer(binding.remoteView)
            }

            observeCallStatus()
        }

        binding.btnEndCall.setOnClickListener {
            callViewModel.endCall()
            finish()
        }
    }

    private fun setupVideoViews(
        localView: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
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
                            if (!hasEnded) {
                                hasEnded = true
//                                Handler(Looper.getMainLooper()).postDelayed({
//                                    finish()
//                                }, 1000) // wait 1 second before closing
                            }
                        }

                        "Initializing" -> {
                            // Optional: show loading
                        }

                        "Incoming Call", "Answered", "Call Connected" -> {
                            // Keep the screen active
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