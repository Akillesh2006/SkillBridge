package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssessmentQuestionDto(
    val id: Long,
    val skillName: String,
    val difficulty: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int? = null,
    val studentAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val explanation: String? = null,
    val concept: String? = null,
    val questionType: String? = null
)

@Serializable
data class SkillVerificationDetailDto(
    val skillName: String,
    val totalQuestions: Int,
    val correctQuestions: Int,
    val percentage: Int,
    val status: String
)

@Serializable
data class AssessmentResponse(
    val id: Long,
    val score: Int,
    val createdDate: String,
    val questions: List<AssessmentQuestionDto>,
    val skillBreakdown: List<SkillVerificationDetailDto> = emptyList(),
    val assessmentType: String = "VERIFICATION",
    val masteryLevel: String? = null,
    val strengths: String? = null,
    val weakConcepts: String? = null,
    val aiFeedback: String? = null,
    val recommendedPractice: String? = null,
    val conceptualScore: Int? = null,
    val applicationScore: Int? = null,
    val problemSolvingScore: Int? = null,
    val debuggingScore: Int? = null
)

@Serializable
data class AnswerSubmissionDto(
    val questionId: Long,
    val studentAnswer: Int
)

@Serializable
data class AssessmentHistoryDto(
    val id: Long,
    val score: Int,
    val createdDate: String
)
