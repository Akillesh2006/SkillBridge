package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.ActivityItem
import com.skillbridge.ai.domain.repository.DashboardRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import com.skillbridge.ai.domain.repository.AssessmentRepository
import com.skillbridge.ai.domain.repository.SkillGapRepository
import com.skillbridge.ai.domain.repository.CopilotRepository
import com.skillbridge.ai.domain.repository.CareerMissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
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
    val preparationMatch: Int = 50,
    val careerMissionTitle: String? = null,
    val careerMissionScenario: String? = null,
    val careerMissionDifficulty: String? = null,
    val careerMissionScore: Int? = null,
    val isCareerMissionCompleted: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val profileRepository: ProfileRepository,
    private val assessmentRepository: AssessmentRepository,
    private val skillGapRepository: SkillGapRepository,
    private val copilotRepository: CopilotRepository,
    private val careerMissionRepository: CareerMissionRepository
) : BaseViewModel<DashboardUiData>() {

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                // Fetch independent dashboard sections concurrently in parallel
                val profileDeferred = async { profileRepository.getUserProfile() }
                val percentageDeferred = async { dashboardRepository.getProgressPercentage() }
                val activitiesDeferred = async { dashboardRepository.getRecentActivities() }
                val assessmentHistoryDeferred = async { assessmentRepository.getAssessmentHistory() }
                val skillGapDeferred = async { skillGapRepository.getSkillGapAnalysis() }
                val dailyMissionDeferred = async { copilotRepository.getDailyMission() }
                val activeMissionDeferred = async { careerMissionRepository.getActiveMission() }
                val careerReadinessDeferred = async { copilotRepository.getCareerReadiness() }

                val profile = profileDeferred.await()
                val welcome = dashboardRepository.getWelcomeMessage(profile.username)
                val percentage = percentageDeferred.await()
                val activities = activitiesDeferred.await()

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

                var cmTitle: String? = null
                var cmScenario: String? = null
                var cmDiff: String? = null
                var cmScore: Int? = null
                var cmCompleted = false

                try {
                    val historyResult = assessmentHistoryDeferred.await()
                    historyResult.onSuccess { history ->
                        if (history.isNotEmpty()) {
                            latestScore = history.first().score
                            latestId = history.first().id
                        }
                    }
                    val skillGap = skillGapDeferred.await()
                    totalSkills = skillGap.skillDetails.size
                    verifiedSkills = skillGap.skillDetails.count { it.status == "VERIFIED" }
                    if (skillGap.readinessScore > 0) {
                        prepMatchVal = skillGap.readinessScore
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard secondary metrics failed: ${e.message}")
                }

                try {
                    val missionResult = dailyMissionDeferred.await()
                    missionResult.onSuccess { mission ->
                        missionFocus = mission.focusSkill
                        missionReason = mission.reason
                        missionTasks = mission.tasks
                        missionTime = mission.estimatedTime
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard mission fetch failed: ${e.message}")
                }

                try {
                    val cmResult = activeMissionDeferred.await()
                    cmResult.onSuccess { cm ->
                        cmTitle = cm.title
                        cmScenario = cm.scenario
                        cmDiff = cm.difficulty
                        cmScore = cm.score
                        cmCompleted = cm.completed
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard career mission fetch failed: ${e.message}")
                }

                try {
                    val readinessResult = careerReadinessDeferred.await()
                    readinessResult.onSuccess { readiness ->
                        if (readiness.overallScore > 0) {
                            prepMatchVal = readiness.overallScore
                        }
                    }
                } catch (e: Exception) {
                    System.err.println("Dashboard readiness fetch failed: ${e.message}")
                }

                val duration = System.currentTimeMillis() - startTime
                android.util.Log.d("SkillBridgePerf", "[PERF] Dashboard load (parallel) = $duration ms")

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
                        preparationMatch = prepMatchVal,
                        careerMissionTitle = cmTitle,
                        careerMissionScenario = cmScenario,
                        careerMissionDifficulty = cmDiff,
                        careerMissionScore = cmScore,
                        isCareerMissionCompleted = cmCompleted
                    )
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load dashboard data", e)
            }
        }
    }
}
