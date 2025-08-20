package com.example.firebasewebrtc.data.api

import com.example.firebasewebrtc.data.model.NotificationRequestDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IApiService {
    @Headers("Accept: application/json")
    @POST("send-call-notification")
    fun requestNotification(@Body request: NotificationRequestDTO): Call<NotificationRequestDTO>
}