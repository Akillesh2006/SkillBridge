package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.data.remote.dto.*

interface InterviewRepository {
    suspend fun startInterview(): Result<InterviewStartResponse>
    suspend fun submitAnswer(id: Long, answer: String): Result<InterviewQuestionResponse>
    suspend fun getResult(id: Long): Result<InterviewResultResponse>
    suspend fun getHistory(): Result<List<InterviewResultResponse>>
}
