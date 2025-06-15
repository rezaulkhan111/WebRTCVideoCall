package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callerId: String
    private lateinit var callId: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        callerId = intent.getStringExtra("callerId") ?: ""
        callId = intent.getStringExtra("callId") ?: ""
//        if (callId.isEmpty()) {
//            Toast.makeText(this, "Caller ID not found", Toast.LENGTH_SHORT).show()
//            finish()
//        }

        val tvIncomingFrom: TextView = findViewById<TextView>(R.id.tvIncomingFrom)
        val btnAccept: Button = findViewById<Button>(R.id.btnAccept)
        val btnReject: Button = findViewById<Button>(R.id.btnReject)

        tvIncomingFrom.text = "Incoming call from: $callerId"

        btnAccept.setOnClickListener {
            val intent = Intent(this, ReceivedCallActivity::class.java)
//            intent.putExtra("callerIdData", callerId)
            intent.putExtra("callId", callId)
            intent.putExtra("isCaller", false) // Not the caller
            startActivity(intent)
            finish()
        }

        btnReject.setOnClickListener {
            finish() // Just close the screen
        }
    }
}