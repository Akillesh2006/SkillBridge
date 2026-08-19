package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.repository.CopilotRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class AnalyticsUiData(
    val targetJob: String = "",
    val targetCompany: String = "",
    val overallScore: Int = 0,
    val resumeMatch: Int = 0,
    val verifiedScore: Int = 0,
    val roadmapProgress: Int = 0,
    val interviewScore: Int = 0,
    val challengeScore: Int = 0,
    val masteryScore: Int = 0,
    val reportJson: String = ""
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val copilotRepository: CopilotRepository,
    private val profileRepository: ProfileRepository
) : BaseViewModel<AnalyticsUiData>() {

    init {
        loadAnalyticsData()
    }

    fun loadAnalyticsData() {
        viewModelScope.launch {
            setLoading()
            try {
                val profile = profileRepository.getUserProfile()
                var data = AnalyticsUiData(
                    targetJob = profile.targetJob.ifBlank { "Android Developer" },
                    targetCompany = profile.targetCompany.ifBlank { "Any company" }
                )
                
                copilotRepository.getCareerReadiness().onSuccess { response ->
                    data = data.copy(
                        overallScore = response.overallScore,
                        resumeMatch = response.resumeMatch ?: 0,
                        verifiedScore = response.verifiedScore,
                        roadmapProgress = response.roadmapProgress,
                        interviewScore = response.interviewScore,
                        challengeScore = response.challengeScore,
                        masteryScore = response.masteryScore ?: 0,
                        reportJson = response.reportJson ?: ""
                    )
                }
                setSuccess(data)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load analytics data", e)
            }
        }
    }
}
