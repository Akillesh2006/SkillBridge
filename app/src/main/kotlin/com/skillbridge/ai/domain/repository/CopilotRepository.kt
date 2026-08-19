package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.data.remote.dto.*

interface CopilotRepository {
    suspend fun getCopilotContext(): Result<CopilotContextResponse>
    suspend fun chatCopilot(message: String): Result<ChatResponse>
    suspend fun getDailyMission(): Result<DailyMissionResponse>
    suspend fun getCareerReadiness(): Result<CareerReadinessResponse>
    suspend fun getReminderSettings(): Result<ReminderSettingsRequest>
    suspend fun updateReminderSettings(settings: ReminderSettingsRequest): Result<MessageResponse>
}
