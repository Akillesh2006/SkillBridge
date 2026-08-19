package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.ActivityItem

/**
 * Dashboard repository interface for retrieving landing page details.
 */
interface DashboardRepository : BaseRepositoryInterface {
    suspend fun getWelcomeMessage(username: String): String
    suspend fun getProgressPercentage(): Int
    suspend fun getRecentActivities(): List<ActivityItem>
}
