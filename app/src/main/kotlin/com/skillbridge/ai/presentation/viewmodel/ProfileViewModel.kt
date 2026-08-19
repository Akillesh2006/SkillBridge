package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.UserProfile
import com.skillbridge.ai.domain.repository.AuthRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiData(
    val profile: UserProfile = UserProfile("", "", "", "", "", ""),
    val isDarkModeEnabled: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : BaseViewModel<ProfileUiData>() {

    private var currentData = ProfileUiData()

    private val _isDarkThemeGlobal = MutableStateFlow(false)
    val isDarkThemeGlobal: StateFlow<Boolean> = _isDarkThemeGlobal.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            setLoading()
            try {
                val userProfile = profileRepository.getUserProfile()
                currentData = currentData.copy(profile = userProfile)
                setSuccess(currentData)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load user profile", e)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        currentData = currentData.copy(isDarkModeEnabled = enabled)
        _isDarkThemeGlobal.value = enabled
        setSuccess(currentData)
    }

    fun updateProfile(username: String, university: String, targetJob: String, targetCompany: String) {
        viewModelScope.launch {
            currentData = currentData.copy(isSaving = true)
            setSuccess(currentData)

            val updated = currentData.profile.copy(
                username = username,
                university = university,
                targetJob = targetJob,
                targetCompany = targetCompany
            )

            profileRepository.updateUserProfile(updated)
                .onSuccess {
                    currentData = currentData.copy(profile = updated, isSaving = false)
                    setSuccess(currentData)
                }
                .onFailure {
                    currentData = currentData.copy(isSaving = false)
                    setSuccess(currentData)
                    setError(it.message ?: "Failed to save profile changes")
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading()
            authRepository.logout()
                .onSuccess {
                    currentData = currentData.copy(isLoggedOut = true)
                    setSuccess(currentData)
                    onSuccess()
                }
                .onFailure {
                    setError(it.message ?: "Failed to logout")
                }
        }
    }
}
