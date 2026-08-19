package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.ChatRequest
import com.skillbridge.ai.domain.repository.ChatRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChatRepository {

    override suspend fun sendMessage(message: String): Result<String> {
        return try {
            val response = apiService.chat(ChatRequest(message))
            Result.success(response.message)
        } catch (e: HttpException) {
            val code = e.code()
            val errorMsg = when (code) {
                401 -> "Your session has expired. Please login again."
                403 -> "You are not authorized to use SkillBridge AI."
                500 -> "SkillBridge AI encountered a server error. Please try again."
                else -> "HTTP $code: SkillBridge AI encountered an error."
            }
            android.util.Log.e("ChatRepository", "HTTP error $code: ${e.message()}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: IOException) {
            android.util.Log.e("ChatRepository", "Network error: ${e.message}", e)
            Result.failure(Exception("Unable to connect to SkillBridge server."))
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Chat API error: ${e.message}", e)
            Result.failure(Exception("SkillBridge AI is temporarily unavailable. Please try again."))
        }
    }
}
