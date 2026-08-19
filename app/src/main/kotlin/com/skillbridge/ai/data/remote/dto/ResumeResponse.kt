package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResumeResponse(
    val id: Long,
    val fileName: String,
    val filePath: String,
    val extractedText: String,
    val aiAnalysis: String? = null,
    val user: ResumeUserResponse? = null
)

@Serializable
data class ResumeUserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val college: String
)