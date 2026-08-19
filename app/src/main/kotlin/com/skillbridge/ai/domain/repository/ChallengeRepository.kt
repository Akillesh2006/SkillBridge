package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.data.remote.dto.*

interface ChallengeRepository {
    suspend fun generateChallenge(): Result<ChallengeGenerateResponse>
    suspend fun submitChallengeAnswer(id: Long, answer: String): Result<ChallengeResultResponse>
    suspend fun getHistory(): Result<List<ChallengeResultResponse>>
}
