package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.RoadmapWeek

/**
 * Learning roadmap repository interface.
 */
interface RoadmapRepository : BaseRepositoryInterface {
    suspend fun getWeeklyRoadmap(): List<RoadmapWeek>
    suspend fun generateRoadmap(roleName: String): Result<List<RoadmapWeek>>
    suspend fun toggleTaskCompletion(taskId: Long, completed: Boolean): Result<List<RoadmapWeek>>
    suspend fun toggleWeekCompletion(weekNumber: Int, completed: Boolean): Result<List<RoadmapWeek>>
}
