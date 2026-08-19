package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.domain.model.UserProfile

/**
 * User Profile repository interface.
 */
interface ProfileRepository : BaseRepositoryInterface {
    suspend fun getUserProfile(): UserProfile
    suspend fun updateUserProfile(profile: UserProfile): Result<Boolean>
}
