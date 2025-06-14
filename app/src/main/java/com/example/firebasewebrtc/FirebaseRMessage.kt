package com.example.firebasewebrtc

import com.google.gson.annotations.SerializedName


class FirebaseRMessage(
    val callId: String? = null,
    val calleeId: String? = null,
    val title: String? = null,
    val body: String? = null
)

class FirebaseBundle {
    @SerializedName("mMap")
    val mMap: FirebaseMMap? = null
}

class FirebaseMMap {
    @SerializedName("google.delivered_priority")
    val googleDeliveredPriority: String? = null

    @SerializedName("google.sent_time")
    val googleSentTime: Long? = null

    @SerializedName("google.ttl")
    val googleTtl: Int? = null

    @SerializedName("google.original_priority")
    val googleOriginalPriority: String? = null

    @SerializedName("google.product_id")
    val googleProductId: Int? = null

    @SerializedName("body")
    val body: String? = null

    @SerializedName("from")
    val from: String? = null

    @SerializedName("title")
    val title: String? = null

    @SerializedName("google.message_id")
    private val googleMessageId: String? = null

    @SerializedName("google.c.sender.id")
    val googleCSenderId: String? = null
}