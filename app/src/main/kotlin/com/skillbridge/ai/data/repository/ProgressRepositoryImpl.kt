package com.skillbridge.ai.data.repository

import com.skillbridge.ai.domain.model.ProgressMetrics
import com.skillbridge.ai.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class ProgressRepositoryImpl @Inject constructor() : ProgressRepository {

    override suspend fun getProgressMetrics(): ProgressMetrics {
        delay(800)
        return ProgressMetrics(
            overallCompletion = 68,
            streakDays = 12,
            tasksCompleted = 18,
            totalTasks = 26,
            weeklyStudyHours = listOf(2.5f, 4.0f, 3.2f, 5.5f, 1.5f, 6.0f, 4.8f) // Mon-Sun hours
        )
    }
}
