package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeGenerateResponse(
    val challengeId: Long,
    val skillName: String,
    val challengeType: String,
    val questionText: String,
    val options: String
)

@Serializable
data class ChallengeSubmitRequest(
    val answer: String
)

@Serializable
data class ChallengeResultResponse(
    val id: Long,
    val skillName: String,
    val challengeType: String,
    val questionText: String,
    val options: String,
    val correctAnswer: String? = null,
    val studentAnswer: String? = null,
    val correct: Boolean? = null,
    val explanation: String? = null,
    val improvementSuggestion: String? = null,
    val completed: Boolean,
    val createdAt: String
)
