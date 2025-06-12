package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

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
            val callerId: String = etInputCallId.text.toString()
            if (!callerId.isNullOrEmpty()) {
                startActivity(Intent(this, VideoCallActivity::class.java).apply {
                    putExtra("callerIdData", callerId.toString())
                    putExtra("isCaller", isCallStatus)
                })
            }
        }
    }
}