package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SkillDetailDto(
    val skillName: String,
    val status: String,
    val score: Int? = null
)

@Serializable
data class SkillGapResponse(
    val roleName: String,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val matchPercentage: Double,
    val readinessScore: Double,
    val skillDetails: List<SkillDetailDto> = emptyList()
)
