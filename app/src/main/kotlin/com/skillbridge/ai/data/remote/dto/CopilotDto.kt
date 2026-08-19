package com.skillbridge.ai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifiedSkillDto(
    val name: String,
    val status: String,
    val score: Int
)

@Serializable
data class MissedTaskDto(
    val id: Long,
    val title: String,
    val dueDate: String
)

@Serializable
data class CopilotContextResponse(
    val name: String,
    val targetRole: String,
    val roadmapProgress: Int,
    val verifiedSkills: List<VerifiedSkillDto>,
    val skillGaps: List<String>,
    val missedTasks: List<MissedTaskDto>
)

@Serializable
data class DailyMissionResponse(
    val focusSkill: String,
    val reason: String,
    val tasks: List<String>,
    val estimatedTime: Int
)

@Serializable
data class CareerReadinessResponse(
    val technicalScore: Int,
    val verifiedScore: Int,
    val roadmapProgress: Int,
    val interviewScore: Int,
    val challengeScore: Int,
    val overallScore: Int,
    val reportJson: String,
    val resumeMatch: Int? = 0,
    val masteryScore: Int? = 0
)

@Serializable
data class ReminderSettingsRequest(
    val notificationsEnabled: Boolean,
    val reminderTime: String,
    val quietHoursStart: String,
    val quietHoursEnd: String
)

@Serializable
data class MessageResponse(
    val message: String
)
