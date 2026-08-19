package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.UpdateProfileRequestDto
import com.skillbridge.ai.domain.model.UserProfile
import com.skillbridge.ai.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProfileRepository {

    private var cachedProfile: UserProfile? = null

    override suspend fun getUserProfile(): UserProfile {
        val dto = apiService.getUserProfile()
        val domainModel = UserProfile(
            username = dto.fullName,
            email = dto.email,
            university = dto.college,
            targetJob = dto.targetJob ?: "Android Developer",
            targetCompany = dto.targetCompany ?: "Any company",
            avatarUrl = "",
            targetSkills = dto.targetSkills,
            recommendedSkills = dto.recommendedSkills,
            interviewTopics = dto.interviewTopics,
            companyPreparation = dto.companyPreparation
        )
        cachedProfile = domainModel
        return domainModel
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Boolean> {
        return try {
            val reqDto = UpdateProfileRequestDto(
                fullName = profile.username,
                college = profile.university,
                targetJob = profile.targetJob,
                targetCompany = profile.targetCompany
            )
            val dto = apiService.updateUserProfile(reqDto)
            val updated = UserProfile(
                username = dto.fullName,
                email = dto.email,
                university = dto.college,
                targetJob = dto.targetJob ?: "Android Developer",
                targetCompany = dto.targetCompany ?: "Any company",
                avatarUrl = "",
                targetSkills = dto.targetSkills,
                recommendedSkills = dto.recommendedSkills,
                interviewTopics = dto.interviewTopics,
                companyPreparation = dto.companyPreparation
            )
            cachedProfile = updated
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
