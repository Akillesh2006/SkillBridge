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

    private fun mapRoadmaps(roadmaps: List<com.skillbridge.ai.data.remote.dto.RoadmapResponse>): List<RoadmapWeek> {
        val roadmap = roadmaps.maxByOrNull { it.id }
            ?: return emptyList()

        return roadmap.tasks
            .groupBy { it.weekNumber }
            .toSortedMap()
            .map { (weekNumber, tasks) ->
                val firstTask = tasks.first()
                val resources = tasks.map { task ->
                    LearningResource(
                        id = task.id.toString(),
                        title = task.title,
                        type = "Article",
                        duration = "",
                        url = task.resourceUrl
                    )
                }

                val domainTasks = tasks.map { task ->
                    com.skillbridge.ai.domain.model.RoadmapTask(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        resourceUrl = task.resourceUrl,
                        completed = task.completed,
                        status = task.status,
                        lastAssessmentId = task.lastAssessmentId
                    )
                }

                RoadmapWeek(
                    weekNumber = weekNumber,
                    topic = firstTask.skillName,
                    details = tasks.joinToString("\n\n") {
                        it.description
                    },
                    resources = resources,
                    completed = tasks.all { it.completed },
                    tasks = domainTasks
                )
            }
    }

    override suspend fun getWeeklyRoadmap(): List<RoadmapWeek> {
        val roadmaps = apiService.getRoadmaps()
        return mapRoadmaps(roadmaps)
    }

    override suspend fun generateRoadmap(roleName: String): Result<List<RoadmapWeek>> {
        return try {
            val roadmapResponse = apiService.generateRoadmap(roleName)
            val weeks = mapRoadmaps(listOf(roadmapResponse))
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

            Result.success(mapRoadmaps(updatedRoadmaps))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}