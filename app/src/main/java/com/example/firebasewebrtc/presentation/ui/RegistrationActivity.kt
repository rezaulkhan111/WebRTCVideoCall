package com.example.firebasewebrtc.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.firebasewebrtc.databinding.ActivityRegistrationBinding
import com.example.firebasewebrtc.presentation.viewmodel.CallingVM
import com.example.firebasewebrtc.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: CallingVM by viewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val fireMessage = FirebaseMessaging.getInstance().token

    private var mCurrentCallId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.fcmCallerId.collect { currentCallId ->
                if (currentCallId != null) {
                    mCurrentCallId = currentCallId
                }
            }
        }

        if (mCurrentCallId.isNullOrEmpty()) {
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

//                                    SharedPreferenceUtil.setFCMCallerId(documentId)
//                                    SharedPreferenceUtil.setFCMToken(fcmToken)

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
//            SharedPreferenceUtil.setFCMCallerId(calleeId)
//            SharedPreferenceUtil.setFCMToken(fcmToken)

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