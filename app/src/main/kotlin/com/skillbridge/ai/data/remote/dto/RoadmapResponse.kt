package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapResponse(
    val id: Long? = 1L,
    val roleName: String? = "Software Developer",
    val matchPercentage: Double? = 0.0,
    val readinessScore: Double? = 0.0,
    val createdAt: String? = null,
    val tasks: List<RoadmapTaskResponse> = emptyList()
)