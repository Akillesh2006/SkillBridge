package com.skillbridge.ai.domain.repository

/**
 * Authentication repository interface defining authentication operations.
 */
interface AuthRepository : BaseRepositoryInterface {
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun register(email: String, username: String, password: String): Result<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean>
    suspend fun logout(): Result<Unit>
    suspend fun isLoggedIn(): Boolean
}
