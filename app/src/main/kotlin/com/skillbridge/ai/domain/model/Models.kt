package com.skillbridge.ai.domain.model

/**
 * Represent a recent activity item shown on the dashboard.
 */
data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val category: String // e.g., "Resume", "Skill", "Roadmap", "Progress"
)

/**
 * Represent a learning resource item within a roadmap milestone.
 */
data class LearningResource(
    val id: String,
    val title: String,
    val type: String, // e.g., "Video", "Article", "Course", "Quiz"
    val duration: String,
    val url: String
)

/**
 * Represent a weekly learning roadmap milestone.
 */
data class RoadmapTask(
    val id: Long,
    val title: String,
    val description: String,
    val resourceUrl: String,
    val completed: Boolean,
    val status: String,
    val lastAssessmentId: Long? = null
)

data class RoadmapWeek(
    val weekNumber: Int,
    val topic: String,
    val details: String,
    val resources: List<LearningResource>,
    val completed: Boolean,
    val tasks: List<RoadmapTask> = emptyList()
)

data class SkillDetail(
    val skillName: String,
    val status: String,
    val score: Int? = null
)

/**
 * Represent the skill gap analysis output.
 */
data class SkillGapAnalysis(
    val readinessScore: Int,
    val currentSkills: List<String>,
    val missingSkills: List<String>,
    val recommendedSkills: List<String>,
    val skillComparison: Map<String, Pair<Int, Int>>, // skill name -> Pair(currentScore, targetScore) out of 100
    val skillDetails: List<SkillDetail> = emptyList()
)

/**
 * Represent the overall progress statistics of the user.
 */
data class ProgressMetrics(
    val overallCompletion: Int, // percentage (0-100)
    val streakDays: Int,
    val tasksCompleted: Int,
    val totalTasks: Int,
    val weeklyStudyHours: List<Float> // list of 7 floats representing Mon-Sun hours
)

/**
 * Represent the profile information of the student user.
 */
data class UserProfile(
    val username: String,
    val email: String,
    val university: String,
    val targetJob: String,
    val targetCompany: String,
    val avatarUrl: String,
    val targetSkills: String? = null,
    val recommendedSkills: String? = null,
    val interviewTopics: String? = null,
    val companyPreparation: String? = null
)

data class AssessmentQuestion(
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

data class SkillVerificationDetail(
    val skillName: String,
    val totalQuestions: Int,
    val correctQuestions: Int,
    val percentage: Int,
    val status: String
)

data class Assessment(
    val id: Long,
    val score: Int,
    val createdDate: String,
    val questions: List<AssessmentQuestion>,
    val skillBreakdown: List<SkillVerificationDetail> = emptyList(),
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

data class AssessmentHistoryItem(
    val id: Long,
    val score: Int,
    val createdDate: String
)
