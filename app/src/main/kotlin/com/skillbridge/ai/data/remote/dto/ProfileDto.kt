package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val college: String,
    val targetJob: String? = null,
    val targetCompany: String? = null,
    val targetSkills: String? = null,
    val recommendedSkills: String? = null,
    val interviewTopics: String? = null,
    val companyPreparation: String? = null
)

@Serializable
data class UpdateProfileRequestDto(
    val fullName: String,
    val college: String,
    val targetJob: String? = null,
    val targetCompany: String? = null
)
