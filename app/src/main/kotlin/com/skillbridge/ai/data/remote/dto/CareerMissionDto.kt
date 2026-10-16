package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectTaskItem(
    val id: Int,
    val title: String,
    val description: String = "",
    val status: String = "NOT_STARTED" // "COMPLETED", "IN_PROGRESS", "NOT_STARTED"
)

@Serializable
data class CareerMissionResponse(
    val id: Long,
    val targetJob: String? = null,
    val title: String? = null,
    val scenario: String? = null,
    val taskInstructions: String? = null,
    val expectedResponseType: String? = null,
    val difficulty: String? = null,
    val estimatedMinutes: Int? = null,
    val status: String? = "GENERATED",
    val whyThisMission: String? = null,
    val completed: Boolean = false,
    val studentResponse: String? = null,
    val githubUrl: String? = null,
    val score: Int? = null,
    val requirementsScore: Int? = null,
    val technicalScore: Int? = null,
    val problemSolvingScore: Int? = null,
    val clarityScore: Int? = null,
    val architectureScore: Int? = null,
    val codeQualityScore: Int? = null,
    val technologyScore: Int? = null,
    val testingScore: Int? = null,
    val documentationScore: Int? = null,
    val careerRelevanceScore: Int? = null,
    val recommendedNextStep: String? = null,
    val feedback: String? = null,
    val skillsEvaluated: List<String> = emptyList(),
    val projectRequirements: List<String> = emptyList(),
    val evaluationCriteria: List<String> = emptyList(),
    val completedRequirements: List<String> = emptyList(),
    val missingRequirements: List<String> = emptyList(),
    val strengths: List<String> = emptyList(),
    val needsImprovement: List<String> = emptyList(),
    val missingConcepts: List<String> = emptyList(),
    val tasks: List<ProjectTaskItem> = emptyList(),
    val createdAt: String? = null,
    val completedAt: String? = null
)

@Serializable
data class MissionSubmitRequest(
    val githubUrl: String? = null,
    val response: String? = null
)
