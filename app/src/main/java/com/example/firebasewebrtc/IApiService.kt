package com.example.firebasewebrtc

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IApiService {
    @Headers("Accept: application/json")
    @POST("send-call-notification")
    fun requestNotification(@Body request: NotificationRequest): Call<NotificationRequest>
}