package com.example.firebasewebrtc

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientInstance {
    private var retrofit: Retrofit? = null

    fun getRetrofitInstance(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl("http://192.168.0.110:6060/api/Notification/")
                .client(provideOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().callTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            ).addInterceptor(Interceptor { chain ->
                val request: Request = chain.request()
                val response = chain.proceed(request)
                response
            }).build()
    }
}