package com.example.firebasewebrtc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasewebrtc.databinding.ActivityRegistrationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnSignUp.setOnClickListener {
                val calleeId = tietNumberInput.text.toString()
                if (!calleeId.isNullOrEmpty()) {
                    calleeId.toString()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("FCM", "Fetching FCM token failed", task.exception)
                            return@addOnCompleteListener
                        }

                        val fcmToken = task.result
                        SharedPreferenceUtil.setFCMToken(fcmToken)
                        // Now save both calleeId and token to Firestore
                        val userData = hashMapOf(
                            "calleeId" to calleeId,
                            "fcmToken" to fcmToken,
                            "sdp" to "",
                            "type" to ""
                        )

                        FirebaseFirestore.getInstance()
                            .collection(AppConstants.FCM_collection)
                            .document(calleeId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("FCM", "FCM Token saved for user: $calleeId")
                            }.addOnFailureListener {
                                Log.e("FCM", "Error saving token", it)
                            }
                    }
                }
            }
        }
    }
}