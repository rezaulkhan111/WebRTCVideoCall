package com.example.firebasewebrtc.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.databinding.ActivityCallListBinding
import com.example.firebasewebrtc.domain.model.UserInfoDetails
import com.example.firebasewebrtc.presentation.adapter.UserAdapter
import com.example.firebasewebrtc.presentation.adapter.UserInteraction
import com.example.firebasewebrtc.presentation.base.BaseActivity
import com.example.firebasewebrtc.presentation.viewmodel.CallingVM
import com.example.firebasewebrtc.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class CallListActivity : BaseActivity(), UserInteraction {

    private lateinit var binding: ActivityCallListBinding
    private lateinit var adapterUser: UserAdapter

    private val viewModel: CallingVM by viewModels()

    private val firestore = FirebaseFirestore.getInstance()

    private var lsUserData = mutableListOf<UserInfoDetails>()

    @SuppressLint("ImplicitSamInstance")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            rvUserList.apply {
                layoutManager =
                    LinearLayoutManager(this@CallListActivity, RecyclerView.VERTICAL, false)
                adapterUser = UserAdapter(this@CallListActivity)
                adapter = adapterUser
            }
        }


        lifecycleScope.launch {
            viewModel.fcmCallerId.collect { currentCallId ->
                if (currentCallId != null) {
                    fetchUserList(currentCallId)
                }
            }
        }
    }

    private fun fetchUserList(currentCallId: String) {
        firestore.collection(AppConstants.FCM_collection).get()
            .addOnSuccessListener { querySnapshot ->
                lsUserData.clear() // Clear previous data
                querySnapshot.forEach { documentId ->
                    if (documentId.id != currentCallId) {
                        val localFcmToken = documentId.getString("fcmToken")
                        lsUserData.add(
                            UserInfoDetails(
                                calleeId = documentId.id, fcmToken = localFcmToken
                            )
                        )
                    }
                }
                adapterUser.setWorkingAreas(lsUserData)
            }.addOnFailureListener { exception ->
                // Handle the failure
            }
    }

    override fun onClickAudioCall(workingArea: UserInfoDetails) {
        if (!workingArea.calleeId.isNullOrEmpty()) {
            startActivity(Intent(this, CallActivity::class.java).apply {
                putExtra("callId", workingArea.calleeId)
                putExtra("isCaller", true)
                putExtra(AppConstants.isAudioOrVideo, false)
            })
        }
    }

    override fun onClickVideoCall(workingArea: UserInfoDetails) {
        if (!workingArea.calleeId.isNullOrEmpty()) {
            startActivity(Intent(this, CallActivity::class.java).apply {
                putExtra("callId", workingArea.calleeId)
                putExtra("isCaller", true)
                putExtra(AppConstants.isAudioOrVideo, true)
            })
        }
    }

}