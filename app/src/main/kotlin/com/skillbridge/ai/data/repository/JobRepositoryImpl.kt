package com.skillbridge.ai.data.repository

import com.skillbridge.ai.domain.repository.JobRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val profileRepository: ProfileRepository
) : JobRepository {

    private val jobs = listOf(
        "Java Backend Developer",
        "Android Developer",
        "Full Stack Developer",
        "Data Scientist",
        "Machine Learning Engineer",
        "Frontend Developer",
        "DevOps Engineer",
        "Cloud Engineer",
        "Cybersecurity Analyst",
        "Data Analyst"
    )

    private val companies = listOf(
        "TCS", "Infosys", "Zoho", "Wipro", "Accenture", "Amazon", "Google", "Microsoft", "Deloitte"
    )

    override suspend fun getPopularJobs(): List<String> {
        return jobs
    }

    override suspend fun getPopularCompanies(): List<String> {
        return companies
    }

    override suspend fun searchJobs(query: String): List<String> {
        if (query.isBlank()) return jobs
        return jobs.filter { it.contains(query, ignoreCase = true) }
    }

    override suspend fun saveTargetJob(jobTitle: String, company: String): Result<Boolean> {
        return try {
            val profile = profileRepository.getUserProfile()
            profileRepository.updateUserProfile(
                profile.copy(
                    targetJob = jobTitle,
                    targetCompany = company
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSavedTargetJob(): Pair<String, String>? {
        return try {
            val profile = profileRepository.getUserProfile()
            Pair(profile.targetJob, profile.targetCompany)
        } catch (e: Exception) {
            Pair("Android Developer", "Google")
        }
    }
}
