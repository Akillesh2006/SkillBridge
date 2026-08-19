package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String
)

@Serializable
data class ChatResponse(
    val message: String
)
