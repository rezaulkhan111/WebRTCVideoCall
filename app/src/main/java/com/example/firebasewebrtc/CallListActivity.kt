package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.databinding.ActivityCallListBinding
import com.google.firebase.firestore.FirebaseFirestore

class CallListActivity : BaseActivity(), UserInteraction {

    private lateinit var binding: ActivityCallListBinding
    private lateinit var adapterUser: UserAdapter

    val firestore = FirebaseFirestore.getInstance()

    var lsUserData = mutableListOf<UserModel>()
    var isCallStatus: Boolean = false

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
                        lsUserData.add(UserModel(calleeId = documentId.id))
                    }
                }
                adapterUser.setWorkingAreas(lsUserData)

            }.addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting document IDs: ", exception)
            }
    }

    override fun onClickCall(workingArea: UserModel) {
        if (isCallStatus) {
            isCallStatus = false
        } else {
            isCallStatus = true
        }

        if (!workingArea.calleeId.isNullOrEmpty()) {
            startActivity(Intent(this, VideoCallActivity::class.java).apply {
                putExtra("callerIdData", workingArea.calleeId.toString())
                putExtra("isCaller", true)
            })
        }
    }
}