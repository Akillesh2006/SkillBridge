package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.domain.model.LearningResource
import com.skillbridge.ai.domain.model.RoadmapWeek
import com.skillbridge.ai.domain.repository.RoadmapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoadmapRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RoadmapRepository {

    @Volatile
    private var cachedRoadmapWeeks: List<RoadmapWeek>? = null

    private fun mapRoadmaps(roadmaps: List<com.skillbridge.ai.data.remote.dto.RoadmapResponse>): List<RoadmapWeek> {
        val roadmap = roadmaps.maxByOrNull { it.id ?: 0L }
            ?: return emptyList()

        return roadmap.tasks
            .groupBy { it.weekNumber }
            .toSortedMap()
            .map { (weekNumber, tasks) ->
                val firstTask = tasks.first()
                val resources = tasks.map { task ->
                    LearningResource(
                        id = (task.id ?: 0L).toString(),
                        title = task.title ?: "Resource",
                        type = "Article",
                        duration = "",
                        url = task.resourceUrl ?: "https://developer.android.com"
                    )
                }

                val domainTasks = tasks.map { task ->
                    com.skillbridge.ai.domain.model.RoadmapTask(
                        id = task.id ?: 0L,
                        title = task.title ?: "Task",
                        description = task.description ?: "",
                        resourceUrl = task.resourceUrl ?: "",
                        completed = task.completed,
                        status = task.status ?: "PENDING",
                        lastAssessmentId = task.lastAssessmentId,
                        skillName = task.skillName ?: "Core Skills"
                    )
                }

                RoadmapWeek(
                    weekNumber = weekNumber,
                    topic = firstTask.skillName ?: "Module $weekNumber",
                    details = tasks.joinToString("\n\n") {
                        it.description ?: ""
                    },
                    resources = resources,
                    completed = tasks.isNotEmpty() && tasks.all { it.completed },
                    tasks = domainTasks
                )
            }
    }

    override suspend fun getWeeklyRoadmap(): List<RoadmapWeek> {
        cachedRoadmapWeeks?.let { if (it.isNotEmpty()) return it }
        val roadmaps = apiService.getRoadmaps()
        val mapped = mapRoadmaps(roadmaps)
        cachedRoadmapWeeks = mapped
        return mapped
    }

    override suspend fun generateRoadmap(roleName: String): Result<List<RoadmapWeek>> {
        return try {
            val roadmapResponse = apiService.generateRoadmap(roleName)
            val weeks = mapRoadmaps(listOf(roadmapResponse))
            cachedRoadmapWeeks = weeks
            Result.success(weeks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleTaskCompletion(
        taskId: Long,
        completed: Boolean
    ): Result<List<RoadmapWeek>> {

        return try {
            val updatedRoadmaps = apiService.toggleTaskCompletion(
                taskId = taskId,
                request = mapOf("completed" to completed)
            )

            val mapped = mapRoadmaps(updatedRoadmaps)
            cachedRoadmapWeeks = mapped
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleWeekCompletion(
        weekNumber: Int,
        completed: Boolean
    ): Result<List<RoadmapWeek>> {

        return try {
            val updatedRoadmaps = apiService.toggleWeekCompletion(
                weekNumber = weekNumber,
                request = mapOf("completed" to completed)
            )

            val mapped = mapRoadmaps(updatedRoadmaps)
            cachedRoadmapWeeks = mapped
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}