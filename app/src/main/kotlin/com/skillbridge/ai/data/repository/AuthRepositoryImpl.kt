package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.local.TokenManager
import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.LoginRequest
import com.skillbridge.ai.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            tokenManager.saveToken(response.token)
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Login failed for email: $email")
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, username: String, password: String): Result<Boolean> {
        return try {
            val responseBody = apiService.register(
                com.skillbridge.ai.data.remote.dto.RegisterRequest(
                    fullName = username,
                    email = email,
                    college = "University",
                    password = password
                )
            )
            val responseText = responseBody.string()
            if (responseText.contains("Successful")) {
                Result.success(true)
            } else {
                Result.failure(Exception(responseText))
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration failed for email: $email")
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        // Implementation for reset password if endpoint exists
        return Result.failure(NotImplementedError("Password reset not yet implemented in API"))
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.deleteToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.isTokenAvailable()
    }
}
