package com.example.firebasewebrtc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasewebrtc.databinding.ActivityRegistrationBinding
import com.example.firebasewebrtc.ui.CallListActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    val firestore = FirebaseFirestore.getInstance()
    val fireMessage = FirebaseMessaging.getInstance().token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!SharedPreferenceUtil.getFCMToken().isNullOrEmpty()) {
            startActivity(Intent(this@RegistrationActivity, CallListActivity::class.java))
            finish()
        } else {
            binding.apply {
                btnSignUp.setOnClickListener {
                    val localCalleeId = tietNumberInput.text.toString()

                    if (localCalleeId.isNotEmpty()) {
                        Log.e(
                            "RegistratAc",
                            "localCalleeId: " + localCalleeId
                        )
                        firestore.collection(AppConstants.FCM_collection).document(localCalleeId)
                            .get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val documentId = document.id
                                    val fcmToken = document.getString("fcmToken")

                                    SharedPreferenceUtil.setFCMCallerId(documentId)
                                    SharedPreferenceUtil.setFCMToken(fcmToken)

                                    startActivity(
                                        Intent(
                                            this@RegistrationActivity, CallListActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {
                                    registerNewUser(localCalleeId)
                                }
                            }.addOnFailureListener { exception ->
                            }
                    }
                }
            }
        }
    }

    private fun registerNewUser(calleeId: String) {
        fireMessage.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            SharedPreferenceUtil.setFCMCallerId(calleeId)
            SharedPreferenceUtil.setFCMToken(fcmToken)

            val userData = mapOf(
                "calleeId" to calleeId, "fcmToken" to fcmToken, "sdp" to null, "type" to null
            )

            firestore.collection(AppConstants.FCM_collection).document(calleeId).set(userData)
                .addOnSuccessListener {
                    startActivity(Intent(this@RegistrationActivity, CallListActivity::class.java))
                    finish()
                }.addOnFailureListener {

                }
        }
    }
}