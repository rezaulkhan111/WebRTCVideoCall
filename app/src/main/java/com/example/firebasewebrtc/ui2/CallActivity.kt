package com.example.firebasewebrtc.ui2

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.firebasewebrtc.BaseActivity
import com.example.firebasewebrtc.databinding.ActivityCallBinding
import com.example.firebasewebrtc.ui.viewmodel.CallingVM
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.SurfaceViewRenderer

class CallActivity : BaseActivity() {

    private lateinit var binding: ActivityCallBinding
    private val callViewModel: CallingVM by viewModels()
    private lateinit var sessionId: String
    private var hasEnded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra("callId").toString()

        if (!sessionId.isNullOrEmpty()) {
            setupVideoViews(
                binding.localView,
                binding.remoteView
            )

            requestPermissionsIfNeeded {
                callViewModel.initCall(sessionId)
                callViewModel.initLocalRenderer(binding.localView)
                callViewModel.setRemoteRenderer(binding.remoteView)
            }

            // Observe call status
            observeCallStatus()
        }

        // End call button
        binding.endCallBtn.setOnClickListener {
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
        lifecycleScope.launchWhenStarted {
            callViewModel.callStatus.collectLatest { status ->
                binding.callStatusText.text = status

                // Auto finish activity on call end
                if (status == "Call Ended") {
                    hasEnded = true
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callViewModel.cleanupCallSession()
        callViewModel.endCall()
    }
}