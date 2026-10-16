package com.skillbridge.ai.data.repository

import com.skillbridge.ai.domain.model.ActivityItem
import com.skillbridge.ai.domain.repository.DashboardRepository
import com.skillbridge.ai.domain.repository.RoadmapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val roadmapRepository: RoadmapRepository
) : DashboardRepository {

    override suspend fun getWelcomeMessage(username: String): String {
        return "Welcome back, $username!"
    }

    override suspend fun getProgressPercentage(): Int {
        return try {
            val weeks = roadmapRepository.getWeeklyRoadmap()
            val allTasks = weeks.flatMap { it.tasks }
            if (allTasks.isEmpty()) 0
            else (allTasks.count { it.completed } * 100) / allTasks.size
        } catch (e: Exception) {
            50
        }
    }

    override suspend fun getRecentActivities(): List<ActivityItem> {
        return listOf(
            ActivityItem(
                id = "1",
                title = "Resume Uploaded",
                description = "Parsed successfully! 15 skills extracted.",
                timestamp = "2 hours ago",
                category = "Resume"
            ),
            ActivityItem(
                id = "2",
                title = "Completed Week 3 Milestones",
                description = "Mastered Reactivity and flows in Kotlin Coroutines.",
                timestamp = "Yesterday",
                category = "Progress"
            ),
            ActivityItem(
                id = "3",
                title = "Job Target Set",
                description = "Target role set to Android Engineer at Google.",
                timestamp = "3 days ago",
                category = "Job"
            ),
            ActivityItem(
                id = "4",
                title = "Skill Gap Generated",
                description = "Readiness score: 74%. Missing Jetpack Compose testing skills.",
                timestamp = "3 days ago",
                category = "Skill"
            )
        )
    }
}
