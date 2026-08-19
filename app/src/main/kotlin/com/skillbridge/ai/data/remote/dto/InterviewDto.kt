package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InterviewStartResponse(
    val interviewId: Long,
    val role: String,
    val questionId: Long,
    val questionText: String,
    val questionOrder: Int,
    val difficulty: String,
    val completed: Boolean
)

@Serializable
data class AnswerSubmission(
    val answer: String
)

@Serializable
data class InterviewQuestionResponse(
    val completed: Boolean,
    val questionId: Long? = null,
    val questionText: String? = null,
    val questionOrder: Int? = null,
    val difficulty: String? = null,
    val overallScore: Int? = null,
    val feedback: String? = null
)

@Serializable
data class InterviewQuestionDto(
    val id: Long,
    val questionText: String,
    val studentAnswer: String? = null,
    val feedback: String? = null,
    val technicalAccuracy: Int? = null,
    val conceptUnderstanding: Int? = null,
    val communication: Int? = null,
    val score: Int? = null,
    val questionOrder: Int,
    val difficulty: String
)

@Serializable
data class InterviewResultResponse(
    val id: Long,
    val targetRole: String,
    val overallScore: Int? = null,
    val feedback: String? = null,
    val completed: Boolean,
    val createdAt: String,
    val questions: List<InterviewQuestionDto>
)
