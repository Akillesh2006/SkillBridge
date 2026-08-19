package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.ChallengeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ChallengeRepository {

    override suspend fun generateChallenge(): Result<ChallengeGenerateResponse> {
        return try {
            val response = apiService.generateDailyChallenge()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitChallengeAnswer(id: Long, answer: String): Result<ChallengeResultResponse> {
        return try {
            val response = apiService.submitChallengeAnswer(id, ChallengeSubmitRequest(answer))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistory(): Result<List<ChallengeResultResponse>> {
        return try {
            val response = apiService.getChallengeHistory()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
