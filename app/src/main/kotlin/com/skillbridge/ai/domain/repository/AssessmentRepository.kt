package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.Assessment
import com.skillbridge.ai.domain.model.AssessmentHistoryItem

interface AssessmentRepository : BaseRepositoryInterface {
    suspend fun startAssessment(): Result<Assessment>
    suspend fun getAssessment(assessmentId: Long): Result<Assessment>
    suspend fun submitAssessment(assessmentId: Long, answers: List<Pair<Long, Int>>): Result<Assessment>
    suspend fun getAssessmentResult(assessmentId: Long): Result<Assessment>
    suspend fun getAssessmentHistory(): Result<List<AssessmentHistoryItem>>
    suspend fun startMasteryCheck(taskId: Long): Result<Assessment>
    suspend fun getMasteryCheck(assessmentId: Long): Result<Assessment>
    suspend fun submitMasteryCheck(assessmentId: Long, answers: List<Pair<Long, Int>>): Result<Assessment>
    suspend fun getMasteryCheckResult(assessmentId: Long): Result<Assessment>
    suspend fun retestMasteryCheck(assessmentId: Long): Result<Assessment>
}
