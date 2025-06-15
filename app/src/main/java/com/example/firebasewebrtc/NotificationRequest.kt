package com.example.firebasewebrtc

import com.google.gson.annotations.SerializedName

class NotificationRequest(
    @SerializedName("calleeId") var calleeId: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("body") var body: String? = null,
    @SerializedName("callId") var callId: String? = null,
    @SerializedName("messageId") var messageId: String? = null
)