package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.ProgressMetrics

/**
 * Progress tracking repository interface.
 */
interface ProgressRepository : BaseRepositoryInterface {
    suspend fun getProgressMetrics(): ProgressMetrics
}
