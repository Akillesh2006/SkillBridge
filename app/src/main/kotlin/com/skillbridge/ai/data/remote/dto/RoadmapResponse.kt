package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapResponse(
    val id: Long,
    val roleName: String,
    val matchPercentage: Double,
    val readinessScore: Double,
    val createdAt: String,
    val tasks: List<RoadmapTaskResponse>
)