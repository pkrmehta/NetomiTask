package com.pkdev.netomitask.Model

import com.google.gson.annotations.SerializedName

data class ListConnection(
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    @SerializedName("channel")
    val channelName: String? = null,
    @SerializedName("message")
    val channelLastMessage: String? = null
)