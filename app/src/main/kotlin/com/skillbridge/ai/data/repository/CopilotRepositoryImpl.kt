package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.CopilotRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CopilotRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CopilotRepository {

    override suspend fun getCopilotContext(): Result<CopilotContextResponse> {
        return try {
            val response = apiService.getCopilotContext()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun chatCopilot(message: String): Result<ChatResponse> {
        return try {
            val response = apiService.chatCopilot(ChatRequest(message))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyMission(): Result<DailyMissionResponse> {
        return try {
            val response = apiService.getDailyMission()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCareerReadiness(): Result<CareerReadinessResponse> {
        return try {
            val response = apiService.getCareerReadiness()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReminderSettings(): Result<ReminderSettingsRequest> {
        return try {
            val response = apiService.getReminderSettings()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReminderSettings(settings: ReminderSettingsRequest): Result<MessageResponse> {
        return try {
            val response = apiService.updateReminderSettings(settings)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
