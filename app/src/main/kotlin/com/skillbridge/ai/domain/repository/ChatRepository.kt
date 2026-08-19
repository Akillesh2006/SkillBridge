package com.skillbridge.ai.domain.repository

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<String>
}
