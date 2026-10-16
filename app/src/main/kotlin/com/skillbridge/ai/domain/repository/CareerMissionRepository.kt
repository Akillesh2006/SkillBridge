package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.data.remote.dto.CareerMissionResponse

interface CareerMissionRepository {
    suspend fun getActiveMission(): Result<CareerMissionResponse>
    suspend fun generateMission(): Result<CareerMissionResponse>
    suspend fun startMission(id: Long): Result<CareerMissionResponse>
    suspend fun submitMission(id: Long, responseText: String): Result<CareerMissionResponse>
    suspend fun getMissionHistory(): Result<List<CareerMissionResponse>>
}
