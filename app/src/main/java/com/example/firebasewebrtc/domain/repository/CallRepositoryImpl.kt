package com.example.firebasewebrtc.domain.repository

import android.content.Context
import com.example.firebasewebrtc.data.api.IApiService
import com.example.firebasewebrtc.data.model.NotificationRequestDTO
import com.example.firebasewebrtc.utils.ApiResult
import com.example.firebasewebrtc.utils.isNetworkAvailable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
    private val apiService: IApiService,
    @ApplicationContext private val context: Context
) : ICallRepository {

    override suspend fun requestNotification(request: NotificationRequestDTO?): ApiResult<NotificationRequestDTO> {
        return if (isNetworkAvailable(context)) {
            try {
                val response = apiService.requestNotification(request)
                ApiResult.Success(response)
            } catch (exp: Exception) {
                exp.printStackTrace()
                ApiResult.Error(exp)
            }
        } else {
            ApiResult.Error(Exception("No internet connection"))
        }
    }
}