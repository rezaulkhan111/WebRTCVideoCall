package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.databinding.ActivityCallListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import retrofit2.Callback

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
            startActivity(Intent(this, SendCallActivity::class.java).apply {
                putExtra("callId", workingArea.calleeId.toString())
                putExtra("isCaller", true)
            })
        }
    }

    fun fetchNotification(
        callOrSessionId: String
    ) {
        val dateService =
            RetrofitClientInstance.getRetrofitInstance()?.create(IApiService::class.java)
        val call = dateService?.requestNotification(
            NotificationRequest(
                calleeId = callOrSessionId,
                title = "📞 Incoming Call",
                body = "User ${SharedPreferenceUtil.getFCMCallerId()} is calling you...",
                callId = callOrSessionId
            )
        )
        call!!.enqueue(object : Callback<NotificationRequest?> {
            @SuppressLint("NewApi", "SetTextI18n")
            override fun onResponse(
                call: retrofit2.Call<NotificationRequest?>,
                response: retrofit2.Response<NotificationRequest?>
            ) {
                if (response.isSuccessful) {
                    startActivity(
                        Intent(
                            this@CallListActivity, SendCallActivity::class.java
                        ).apply {
                            putExtra("callId", callOrSessionId)
                            putExtra("isCaller", true)
                        })
                } else {
                    Log.e("CallActivity", "else")
                }
            }

            override fun onFailure(call: retrofit2.Call<NotificationRequest?>, t: Throwable) {
                Log.e("CallActivity", "else: " + t.message)
            }
        })
    }
}