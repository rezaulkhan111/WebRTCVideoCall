package com.example.firebasewebrtc.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasewebrtc.R
import com.example.firebasewebrtc.ui.ReceivedCallActivity

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callerId: String
    private lateinit var callId: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        callerId = intent.getStringExtra("callerId") ?: ""
        callId = intent.getStringExtra("callId") ?: ""

        val tvIncomingFrom: TextView = findViewById(R.id.tvIncomingFrom)
        val btnAccept: Button = findViewById(R.id.btnAccept)
        val btnReject: Button = findViewById(R.id.btnReject)

        tvIncomingFrom.text = "Incoming call from: $callerId"

        btnAccept.setOnClickListener {
            val intent = Intent(this, ReceivedCallActivity::class.java)
            intent.putExtra("callId", callId)
            intent.putExtra("isCaller", false) // Not the caller
            startActivity(intent)
            finish()
        }

        btnReject.setOnClickListener {
            finish()
        }
    }
}