package com.example.firebasewebrtc.data.model

import com.google.gson.annotations.SerializedName

class NotificationRequestDTO(
    @SerializedName("calleeId") var calleeId: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("body") var body: String? = null,
    @SerializedName("callId") var callId: String? = null,
    @SerializedName("callType") var callType: String? = null,
)