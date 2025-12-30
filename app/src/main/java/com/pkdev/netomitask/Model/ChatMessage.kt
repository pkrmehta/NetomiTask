package com.pkdev.netomitask.Model

data class ChatMessage(
    val message: String,
    val isSentByMe: Boolean,
    val isNetworkConnected: Boolean = true
)