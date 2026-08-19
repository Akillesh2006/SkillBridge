package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.ActivityItem
import com.skillbridge.ai.domain.repository.DashboardRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import com.skillbridge.ai.domain.repository.AssessmentRepository
import com.skillbridge.ai.domain.repository.SkillGapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class DashboardUiData(
    val welcomeMessage: String = "",
    val completionPercentage: Int = 0,
    val recentActivities: List<ActivityItem> = emptyList(),
    val studentName: String = "",
    val verificationScore: Int? = null,
    val verifiedSkillsCount: Int = 0,
    val totalSkillsCount: Int = 0,
    val latestAssessmentId: Long? = null,
    val dailyMissionFocus: String? = null,
    val dailyMissionReason: String? = null,
    val dailyMissionTasks: List<String> = emptyList(),
    val dailyMissionTime: Int = 0,
    val targetJob: String = "Android Developer",
    val targetCompany: String = "Google",
    val preparationMatch: Int = 50
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val profileRepository: ProfileRepository,
    private val assessmentRepository: AssessmentRepository,
    private val skillGapRepository: SkillGapRepository,
    private val copilotRepository: com.skillbridge.ai.domain.repository.CopilotRepository
) : BaseViewModel<DashboardUiData>() {

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            setLoading()
            try {
                val profile = profileRepository.getUserProfile()
                val welcome = dashboardRepository.getWelcomeMessage(profile.username)
                val percentage = dashboardRepository.getProgressPercentage()
                val activities = dashboardRepository.getRecentActivities()

                var latestScore: Int? = null
                var latestId: Long? = null
                var verifiedSkills = 0
                var totalSkills = 0

                var missionFocus: String? = null
                var missionReason: String? = null
                var missionTasks: List<String> = emptyList()
                var missionTime = 0

                var targetJobStr = profile.targetJob.ifBlank { "Android Developer" }
                var targetCompStr = profile.targetCompany.ifBlank { "Any company" }
                var prepMatchVal = 50

                try {
                    assessmentRepository.getAssessmentHistory().onSuccess { history ->
                        if (history.isNotEmpty()) {
                            latestScore = history.first().score
                            latestId = history.first().id
                        }
                    }
                    val skillGap = skillGapRepository.getSkillGapAnalysis()
                    totalSkills = skillGap.skillDetails.size
                    verifiedSkills = skillGap.skillDetails.count { it.status == "VERIFIED" }
                } catch (e: Exception) {
                    System.err.println("Dashboard secondary fetches failed: ${e.message}")
                }

                try {
                    copilotRepository.getDailyMission().onSuccess { mission ->
                        missionFocus = mission.focusSkill
                        missionReason = mission.reason
                        missionTasks = mission.tasks
                        missionTime = mission.estimatedTime
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard mission fetch failed: ${e.message}")
                }

                try {
                    copilotRepository.getCareerReadiness().onSuccess { readiness ->
                        prepMatchVal = readiness.overallScore
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard readiness fetch failed: ${e.message}")
                }

                setSuccess(
                    DashboardUiData(
                        welcomeMessage = welcome,
                        completionPercentage = percentage,
                        recentActivities = activities,
                        studentName = profile.username,
                        verificationScore = latestScore,
                        verifiedSkillsCount = verifiedSkills,
                        totalSkillsCount = totalSkills,
                        latestAssessmentId = latestId,
                        dailyMissionFocus = missionFocus,
                        dailyMissionReason = missionReason,
                        dailyMissionTasks = missionTasks,
                        dailyMissionTime = missionTime,
                        targetJob = targetJobStr,
                        targetCompany = targetCompStr,
                        preparationMatch = prepMatchVal
                    )
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load dashboard data", e)
            }
        }
    }
}
