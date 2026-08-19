package com.skillbridge.ai.data.repository

import com.skillbridge.ai.domain.model.ActivityItem
import com.skillbridge.ai.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class DashboardRepositoryImpl @Inject constructor() : DashboardRepository {

    override suspend fun getWelcomeMessage(username: String): String {
        return "Welcome back, $username!"
    }

    override suspend fun getProgressPercentage(): Int {
        return 68
    }

    override suspend fun getRecentActivities(): List<ActivityItem> {
        delay(500)
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
