package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.SkillGapAnalysis

/**
 * Skill gap analysis repository interface.
 */
interface SkillGapRepository : BaseRepositoryInterface {
    suspend fun getSkillGapAnalysis(): SkillGapAnalysis
}
