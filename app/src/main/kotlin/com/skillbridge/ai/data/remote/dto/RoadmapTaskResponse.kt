package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapTaskResponse(
    val id: Long? = 0L,
    val weekNumber: Int = 1,
    val skillName: String? = "Core Skill",
    val title: String? = "Task",
    val description: String? = "",
    val resourceUrl: String? = "",
    val completed: Boolean = false,
    val status: String? = "PENDING",
    val lastAssessmentId: Long? = null
)