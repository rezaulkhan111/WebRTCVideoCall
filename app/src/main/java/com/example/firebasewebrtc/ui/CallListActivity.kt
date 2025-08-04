package com.example.firebasewebrtc.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.AppConstants
import com.example.firebasewebrtc.BaseActivity
import com.example.firebasewebrtc.IApiService
import com.example.firebasewebrtc.NotificationRequest
import com.example.firebasewebrtc.RetrofitClientInstance
import com.example.firebasewebrtc.SharedPreferenceUtil
import com.example.firebasewebrtc.UserAdapter
import com.example.firebasewebrtc.UserInteraction
import com.example.firebasewebrtc.UserModel
import com.example.firebasewebrtc.databinding.ActivityCallListBinding
import com.example.firebasewebrtc.ui2.CallActivity
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallListActivity : BaseActivity(), UserInteraction {

    private lateinit var binding: ActivityCallListBinding
    private lateinit var adapterUser: UserAdapter

    val firestore = FirebaseFirestore.getInstance()

    var lsUserData = mutableListOf<UserModel>()

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

        val currentCallId = SharedPreferenceUtil.getFCMCallerId().toString()
        firestore.collection(AppConstants.FCM_collection).get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.forEach { documentId ->
                    if (documentId.id != currentCallId) {
                        val localFcmToken = documentId.getString("fcmToken")
                        lsUserData.add(
                            UserModel(
                                calleeId = documentId.id, fcmToken = localFcmToken
                            )
                        )
                    }
                }
                adapterUser.setWorkingAreas(lsUserData)
            }.addOnFailureListener { exception ->
            }
    }

    override fun onClickCall(workingArea: UserModel) {
        if (!workingArea.calleeId.isNullOrEmpty()) {
//            fetchNotification(workingArea.calleeId.toString())
            startActivity(Intent(this, CallActivity::class.java).apply {
                putExtra("callId", workingArea.calleeId.toString())
                putExtra("isCaller", true)
            })
        }
    }
}