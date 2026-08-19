package com.skillbridge.ai.domain.repository

/**
 * Job repository interface for selecting target jobs and companies.
 */
interface JobRepository : BaseRepositoryInterface {
    suspend fun getPopularJobs(): List<String>
    suspend fun getPopularCompanies(): List<String>
    suspend fun searchJobs(query: String): List<String>
    suspend fun saveTargetJob(jobTitle: String, company: String): Result<Boolean>
    suspend fun getSavedTargetJob(): Pair<String, String>?
}
