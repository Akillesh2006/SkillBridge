package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.CareerMissionResponse
import com.skillbridge.ai.data.remote.dto.MissionSubmitRequest
import com.skillbridge.ai.domain.repository.CareerMissionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerMissionRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CareerMissionRepository {

    override suspend fun getActiveMission(): Result<CareerMissionResponse> {
        return try {
            val res = apiService.getActiveMission()
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateMission(): Result<CareerMissionResponse> {
        return try {
            val res = apiService.generateMission()
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startMission(id: Long): Result<CareerMissionResponse> {
        return try {
            val res = apiService.startMission(id)
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitMission(id: Long, responseText: String): Result<CareerMissionResponse> {
        return try {
            val res = apiService.submitMission(id, MissionSubmitRequest(githubUrl = responseText, response = responseText))
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMissionHistory(): Result<List<CareerMissionResponse>> {
        return try {
            val res = apiService.getMissionHistory()
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
