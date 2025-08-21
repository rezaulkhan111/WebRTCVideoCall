package com.example.firebasewebrtc.data.model

import com.google.gson.annotations.SerializedName

class NotificationRequestDTO(
    @SerializedName("calleeId") val calleeId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("callId") val callId: String? = null,
    @SerializedName("callType") val callType: String? = null,
)