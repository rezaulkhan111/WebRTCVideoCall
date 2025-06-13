package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.String

class MainActivity : BaseActivity() {

    var isCallStatus: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etInputCallId: EditText = findViewById(R.id.etInputCallId)
        val btnGoCallPage: Button = findViewById(R.id.btnGoCallPage)
        val btnCallStatus: Button = findViewById(R.id.btnCallStatus)
        btnCallStatus.text = "Call Status: $isCallStatus"

        btnCallStatus.setOnClickListener {
            if (isCallStatus) {
                isCallStatus = false
            } else {
                isCallStatus = true
            }

            btnCallStatus.text = "Call Status: $isCallStatus"
        }

        btnGoCallPage.setOnClickListener {
//            val callerId: String = etInputCallId.text.toString()
//            if (!callerId.isNullOrEmpty()) {
//                startActivity(Intent(this, VideoCallActivity::class.java).apply {
//                    putExtra("callerIdData", callerId.toString())
//                    putExtra("isCaller", isCallStatus)
//                })
//            }

            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(OnCompleteListener { task: Task<String?>? ->
                    if (!task!!.isSuccessful) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    }
                    // Get the new FCM token
                    val token: String = task.getResult()!!
                    Log.e("FCM Token", token)
                })
        }
    }
}