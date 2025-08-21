package com.example.firebasewebrtc.domain.repository

import com.example.firebasewebrtc.data.model.NotificationRequestDTO
import com.example.firebasewebrtc.utils.ApiResult
import retrofit2.http.Body

interface ICallRepository {
    suspend fun requestNotification(@Body request: NotificationRequestDTO?): ApiResult<NotificationRequestDTO>
}