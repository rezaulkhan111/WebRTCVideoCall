package com.example.firebasewebrtc

import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object AppConstants {
    const val FCM_collection = "calls"
}