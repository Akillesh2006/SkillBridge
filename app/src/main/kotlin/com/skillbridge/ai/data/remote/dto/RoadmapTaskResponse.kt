package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapTaskResponse(
    val id: Long,
    val weekNumber: Int,
    val skillName: String,
    val title: String,
    val description: String,
    val resourceUrl: String,
    val completed: Boolean,
    val status: String = "PENDING",
    val lastAssessmentId: Long? = null
)