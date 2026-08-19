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
    val completionPercentage: Int = 0
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

    fun loadRoadmap() {
        viewModelScope.launch {
            setLoading()
            try {
                val targetJob = jobRepository.getSavedTargetJob()?.first ?: "Android Developer"
                val result = roadmapRepository.generateRoadmap(targetJob)
                val weeks = result.getOrElse { roadmapRepository.getWeeklyRoadmap() }

                val completedCount = weeks.count { it.completed }
                val percentage = if (weeks.isNotEmpty()) (completedCount * 100) / weeks.size else 0

                currentData = RoadmapUiData(
                    roadmapWeeks = weeks,
                    completionPercentage = percentage
                )
                setSuccess(currentData)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load roadmap", e)
            }
        }
    }

    fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            roadmapRepository.toggleTaskCompletion(taskId, completed)
                .onSuccess { updatedWeeksBackend ->
                    val completedCount = updatedWeeksBackend.count { it.completed }
                    val percentage = if (updatedWeeksBackend.isNotEmpty()) (completedCount * 100) / updatedWeeksBackend.size else 0

                    currentData = RoadmapUiData(
                        roadmapWeeks = updatedWeeksBackend,
                        completionPercentage = percentage
                    )
                    setSuccess(currentData)
                }
                .onFailure { error ->
                    setSuccess(currentData)
                    viewModelScope.launch {
                        _errorFlow.emit(error.message ?: "Unable to update task. Please try again.")
                    }
                }
        }
    }
}
