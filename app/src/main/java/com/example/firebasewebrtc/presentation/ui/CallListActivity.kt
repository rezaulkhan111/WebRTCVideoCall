package com.example.firebasewebrtc.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.databinding.ActivityCallListBinding
import com.example.firebasewebrtc.domain.model.UserInfoDetails
import com.example.firebasewebrtc.presentation.adapter.UserAdapter
import com.example.firebasewebrtc.presentation.adapter.UserInteraction
import com.example.firebasewebrtc.presentation.base.BaseActivity
import com.example.firebasewebrtc.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore

class CallListActivity : BaseActivity(), UserInteraction {

    private lateinit var binding: ActivityCallListBinding
    private lateinit var adapterUser: UserAdapter

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

        val currentCallId ="ddfdf" /*SharedPreferenceUtil.getFCMCallerId().toString()*/
        firestore.collection(AppConstants.FCM_collection).get()
            .addOnSuccessListener { querySnapshot ->
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