package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.RoadmapWeek
import com.skillbridge.ai.domain.repository.JobRepository
import com.skillbridge.ai.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class RoadmapUiData(
    val roadmapWeeks: List<RoadmapWeek> = emptyList(),
    val completionPercentage: Int = 0,
    val targetRole: String = "Android Developer",
    val isGenerating: Boolean = false,
    val generationStage: String = ""
)

@HiltViewModel
class RoadmapViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val jobRepository: JobRepository
) : BaseViewModel<RoadmapUiData>() {

    private var currentData = RoadmapUiData()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    init {
        loadRoadmap()
    }

    private fun calculateOverallPercentage(weeks: List<RoadmapWeek>): Int {
        val allTasks = weeks.flatMap { it.tasks }
        if (allTasks.isEmpty()) {
            val completedWeeks = weeks.count { it.completed }
            return if (weeks.isNotEmpty()) (completedWeeks * 100) / weeks.size else 0
        }
        val completedTasks = allTasks.count { it.completed }
        return (completedTasks * 100) / allTasks.size
    }

    fun loadRoadmap(forceRegenerate: Boolean = false) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val targetJob = jobRepository.getSavedTargetJob()?.first ?: "Android Developer"

            if (forceRegenerate) {
                currentData = currentData.copy(
                    isGenerating = true,
                    generationStage = "Building your personalized roadmap...",
                    targetRole = targetJob
                )
                setSuccess(currentData)

                val result = roadmapRepository.generateRoadmap(targetJob)
                val weeks = result.getOrElse { roadmapRepository.getWeeklyRoadmap() }
                val percentage = calculateOverallPercentage(weeks)

                currentData = RoadmapUiData(
                    roadmapWeeks = weeks,
                    completionPercentage = percentage,
                    targetRole = targetJob,
                    isGenerating = false,
                    generationStage = ""
                )
                setSuccess(currentData)
            } else {
                setLoading()
                try {
                    val existing = roadmapRepository.getWeeklyRoadmap()
                    val weeks: List<RoadmapWeek>

                    if (existing.isNotEmpty()) {
                        weeks = existing
                    } else {
                        val result = roadmapRepository.generateRoadmap(targetJob)
                        weeks = result.getOrElse { emptyList() }
                    }

                    val percentage = calculateOverallPercentage(weeks)

                    currentData = RoadmapUiData(
                        roadmapWeeks = weeks,
                        completionPercentage = percentage,
                        targetRole = targetJob,
                        isGenerating = false,
                        generationStage = ""
                    )
                    setSuccess(currentData)

                    val duration = System.currentTimeMillis() - startTime
                    android.util.Log.d("SkillBridgePerf", "[PERF] Roadmap DB load = $duration ms (Weeks: ${weeks.size})")
                } catch (e: Exception) {
                    setError(e.message ?: "Failed to load roadmap", e)
                }
            }
        }
    }

    fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        // Optimistic local UI update for instant response
        val updatedWeeksOptimistic = currentData.roadmapWeeks.map { week ->
            val updatedTasks = week.tasks.map { task ->
                if (task.id == taskId) task.copy(completed = completed, status = if (completed) "COMPLETED" else "PENDING") else task
            }
            week.copy(
                tasks = updatedTasks,
                completed = updatedTasks.isNotEmpty() && updatedTasks.all { it.completed }
            )
        }

        currentData = currentData.copy(
            roadmapWeeks = updatedWeeksOptimistic,
            completionPercentage = calculateOverallPercentage(updatedWeeksOptimistic)
        )
        setSuccess(currentData)

        viewModelScope.launch {
            roadmapRepository.toggleTaskCompletion(taskId, completed)
                .onSuccess { updatedWeeksBackend ->
                    currentData = currentData.copy(
                        roadmapWeeks = updatedWeeksBackend,
                        completionPercentage = calculateOverallPercentage(updatedWeeksBackend)
                    )
                    setSuccess(currentData)
                }
                .onFailure { error ->
                    // Revert to backend state on failure
                    val freshWeeks = roadmapRepository.getWeeklyRoadmap()
                    currentData = currentData.copy(
                        roadmapWeeks = freshWeeks,
                        completionPercentage = calculateOverallPercentage(freshWeeks)
                    )
                    setSuccess(currentData)
                    _errorFlow.emit(error.message ?: "Unable to update task. Please try again.")
                }
        }
    }

    fun toggleModuleCompleted(weekNumber: Int, completed: Boolean) {
        // Optimistic local update
        val updatedWeeksOptimistic = currentData.roadmapWeeks.map { week ->
            if (week.weekNumber == weekNumber) {
                val updatedTasks = week.tasks.map { it.copy(completed = completed, status = if (completed) "COMPLETED" else "PENDING") }
                week.copy(completed = completed, tasks = updatedTasks)
            } else {
                week
            }
        }

        currentData = currentData.copy(
            roadmapWeeks = updatedWeeksOptimistic,
            completionPercentage = calculateOverallPercentage(updatedWeeksOptimistic)
        )
        setSuccess(currentData)

        viewModelScope.launch {
            roadmapRepository.toggleWeekCompletion(weekNumber, completed)
                .onSuccess { updatedWeeksBackend ->
                    currentData = currentData.copy(
                        roadmapWeeks = updatedWeeksBackend,
                        completionPercentage = calculateOverallPercentage(updatedWeeksBackend)
                    )
                    setSuccess(currentData)
                }
                .onFailure { error ->
                    val freshWeeks = roadmapRepository.getWeeklyRoadmap()
                    currentData = currentData.copy(
                        roadmapWeeks = freshWeeks,
                        completionPercentage = calculateOverallPercentage(freshWeeks)
                    )
                    setSuccess(currentData)
                    _errorFlow.emit(error.message ?: "Unable to update module. Please try again.")
                }
        }
    }
}
