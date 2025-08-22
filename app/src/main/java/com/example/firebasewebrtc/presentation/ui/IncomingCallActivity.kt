package com.example.firebasewebrtc.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasewebrtc.utils.AppConstants
import com.example.firebasewebrtc.data.model.FirebaseRMessageDTO
import com.example.firebasewebrtc.R
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callerId: String
    private lateinit var callId: String
    private var mObjJsonData: FirebaseRMessageDTO? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        callerId = intent.getStringExtra("callerId") ?: ""
        callId = intent.getStringExtra("callId") ?: ""
        val mJsonString = intent.getStringExtra(AppConstants.Common_Transfer_Data)
        mObjJsonData = Gson().fromJson(mJsonString, FirebaseRMessageDTO::class.java)

        val tvIncomingFrom: TextView = findViewById(R.id.tvIncomingFrom)
        val ivCallType: ImageView = findViewById(R.id.ivCallType)
        val btnAccept: Button = findViewById(R.id.btnAccept)
        val btnReject: Button = findViewById(R.id.btnReject)

        tvIncomingFrom.text = "Incoming call from: " + mObjJsonData?.callerNumber

        ivCallType.setImageResource(
            if (mObjJsonData?.callType.toBoolean()) {
                R.drawable.ic_outline_videocam_24
            } else {
                R.drawable.ic_outline_call_24
            }
        )

        btnAccept.setOnClickListener {
            val intent = Intent(this, ReceivedCallActivity::class.java).apply {
                putExtra("callId", callId)
                putExtra("isCaller", false)
                putExtra(AppConstants.isAudioOrVideo, mObjJsonData?.callType.toBoolean())
            }

            startActivity(intent)
            this.finish()
        }

        btnReject.setOnClickListener {
            this.finish()
        }
    }
}