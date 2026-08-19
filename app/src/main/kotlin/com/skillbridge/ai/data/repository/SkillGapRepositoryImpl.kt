package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.domain.model.SkillDetail
import com.skillbridge.ai.domain.model.SkillGapAnalysis
import com.skillbridge.ai.domain.repository.JobRepository
import com.skillbridge.ai.domain.repository.SkillGapRepository
import com.skillbridge.ai.domain.repository.AssessmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillGapRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobRepository: JobRepository,
    private val assessmentRepository: AssessmentRepository
) : SkillGapRepository {

    override suspend fun getSkillGapAnalysis(): SkillGapAnalysis {
        val targetJobPair = jobRepository.getSavedTargetJob()
        val targetRole = targetJobPair?.first ?: "Android Developer"

        return try {
            val response = apiService.analyzeSkillGap(targetRole)
            
            val comparisonMap = mutableMapOf<String, Pair<Int, Int>>()
            response.matchedSkills.forEach { skill ->
                comparisonMap[skill] = Pair(85, 90)
            }
            response.missingSkills.forEach { skill ->
                comparisonMap[skill] = Pair(25, 85)
            }

            SkillGapAnalysis(
                readinessScore = response.readinessScore.toInt(),
                currentSkills = response.matchedSkills,
                missingSkills = response.missingSkills,
                recommendedSkills = emptyList(),
                skillComparison = comparisonMap,
                skillDetails = response.skillDetails.map {
                    SkillDetail(
                        skillName = it.skillName,
                        status = it.status,
                        score = it.score
                    )
                }
            )
        } catch (e: Exception) {
            // Fallback mock if api fails
            val currentSkills = listOf(
                "Java", "SQL", "Git", "Kotlin"
            )

            val missingSkills: List<String>
            val recommendedSkills: List<String>

            if (targetRole.lowercase().contains("data")) {
                missingSkills = listOf("Pandas", "NumPy", "Statistics", "Machine Learning", "Data Visualization", "Scikit-learn")
                recommendedSkills = listOf("Big Data Analytics", "Deep Learning", "Feature Engineering")
            } else if (targetRole.lowercase().contains("front")) {
                missingSkills = listOf("React", "TypeScript", "Redux", "Tailwind CSS")
                recommendedSkills = listOf("Next.js", "Web Performance Optimization", "Accessibility")
            } else {
                missingSkills = listOf("Jetpack Compose", "Android Architecture", "Room Database", "Coroutines")
                recommendedSkills = listOf("CI/CD Pipelines", "Compose Animation", "Clean Architecture")
            }

            val comparisonMap = mutableMapOf<String, Pair<Int, Int>>()
            currentSkills.forEach { skill ->
                comparisonMap[skill] = Pair(85, 90)
            }
            missingSkills.forEach { skill ->
                comparisonMap[skill] = Pair(25, 85)
            }

            // Query local assessment history to update status
            val details = mutableListOf<SkillDetail>()
            val history = assessmentRepository.getAssessmentHistory().getOrNull() ?: emptyList()
            val latestId = history.firstOrNull()?.id
            val latestResult = if (latestId != null) assessmentRepository.getAssessmentResult(latestId).getOrNull() else null

            currentSkills.forEach { skill ->
                val verifiedDetail = latestResult?.skillBreakdown?.find { it.skillName.equals(skill, ignoreCase = true) }
                if (verifiedDetail != null) {
                    details.add(SkillDetail(skill, verifiedDetail.status, verifiedDetail.percentage))
                } else {
                    details.add(SkillDetail(skill, "DETECTED"))
                }
            }
            
            missingSkills.forEach { skill ->
                val verifiedDetail = latestResult?.skillBreakdown?.find { it.skillName.equals(skill, ignoreCase = true) }
                if (verifiedDetail != null) {
                    details.add(SkillDetail(skill, verifiedDetail.status, verifiedDetail.percentage))
                } else {
                    details.add(SkillDetail(skill, "TARGET_SKILL"))
                }
            }

            // Calculate readiness score dynamically based on details
            var matchedWeightSum = 0.0
            val requiredSkills = currentSkills + missingSkills
            
            requiredSkills.forEach { req ->
                val det = details.find { it.skillName.equals(req, ignoreCase = true) }
                if (det != null && det.status != "TARGET_SKILL") {
                    when (det.status) {
                        "VERIFIED" -> matchedWeightSum += 1.0
                        "PARTIALLY_VERIFIED" -> matchedWeightSum += 0.5
                        "NEEDS_IMPROVEMENT" -> matchedWeightSum += 0.2
                        else -> matchedWeightSum += 1.0 // DETECTED
                    }
                }
            }
            
            val dynamicReadiness = if (requiredSkills.isNotEmpty()) {
                (matchedWeightSum / requiredSkills.size) * 100.0
            } else {
                75.0
            }

            SkillGapAnalysis(
                readinessScore = dynamicReadiness.toInt(),
                currentSkills = currentSkills,
                missingSkills = missingSkills,
                recommendedSkills = recommendedSkills,
                skillComparison = comparisonMap,
                skillDetails = details
            )
        }
    }
}
