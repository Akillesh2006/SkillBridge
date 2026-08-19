package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.InterviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : InterviewRepository {

    override suspend fun startInterview(): Result<InterviewStartResponse> {
        return try {
            val response = apiService.startInterview()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAnswer(id: Long, answer: String): Result<InterviewQuestionResponse> {
        return try {
            val response = apiService.submitInterviewAnswer(id, AnswerSubmission(answer))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResult(id: Long): Result<InterviewResultResponse> {
        return try {
            val response = apiService.getInterviewResult(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistory(): Result<List<InterviewResultResponse>> {
        return try {
            val response = apiService.getInterviewHistory()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
