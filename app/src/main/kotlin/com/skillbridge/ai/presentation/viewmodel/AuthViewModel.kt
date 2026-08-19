package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiData(
    val isLoggedIn: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordResetSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthUiData>() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private var currentData = AuthUiData()

    init {
        setSuccess(currentData)
    }

    fun setEmail(value: String) {
        _email.value = value
        clearErrors()
    }

    fun setUsername(value: String) {
        _username.value = value
        clearErrors()
    }

    fun setPassword(value: String) {
        _password.value = value
        clearErrors()
    }

    fun setConfirmPassword(value: String) {
        _confirmPassword.value = value
        clearErrors()
    }

    fun setRememberMe(value: Boolean) {
        _rememberMe.value = value
    }

    private fun clearErrors() {
        currentData = currentData.copy(
            emailError = null,
            passwordError = null,
            confirmPasswordError = null
        )
        setSuccess(currentData)
    }

    fun login(onSuccess: () -> Unit) {
        val emailVal = _email.value
        val passwordVal = _password.value

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (emailVal.isBlank() || !emailVal.contains("@")) {
            emailError = "Please enter a valid email address"
            hasError = true
        }

        if (passwordVal.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }

        if (hasError) {
            currentData = currentData.copy(emailError = emailError, passwordError = passwordError)
            setSuccess(currentData)
            return
        }

        viewModelScope.launch {
            setLoading()
            authRepository.login(emailVal, passwordVal)
                .onSuccess {
                    currentData = currentData.copy(isLoggedIn = true)
                    setSuccess(currentData)
                    onSuccess()
                }
                .onFailure {
                    currentData = currentData.copy(
                        isLoggedIn = false,
                        emailError = it.message ?: "Authentication failed"
                    )
                    setSuccess(currentData)
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val emailVal = _email.value
        val usernameVal = _username.value
        val passwordVal = _password.value
        val confirmPasswordVal = _confirmPassword.value

        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null
        var confirmPassErr: String? = null

        if (emailVal.isBlank() || !emailVal.contains("@")) {
            emailErr = "Please enter a valid email address"
            hasError = true
        }

        if (usernameVal.isBlank()) {
            // Can be extended, but keep it simple
            hasError = true
        }

        if (passwordVal.length < 6) {
            passErr = "Password must be at least 6 characters"
            hasError = true
        }

        if (passwordVal != confirmPasswordVal) {
            confirmPassErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            currentData = currentData.copy(
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmPassErr
            )
            setSuccess(currentData)
            return
        }

        viewModelScope.launch {
            setLoading()
            authRepository.register(emailVal, usernameVal, passwordVal)
                .onSuccess {
                    currentData = currentData.copy(isLoggedIn = true)
                    setSuccess(currentData)
                    onSuccess()
                }
                .onFailure {
                    currentData = currentData.copy(
                        isLoggedIn = false,
                        emailError = it.message ?: "Registration failed"
                    )
                    setSuccess(currentData)
                }
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        val emailVal = _email.value
        if (emailVal.isBlank() || !emailVal.contains("@")) {
            currentData = currentData.copy(emailError = "Please enter a valid email address")
            setSuccess(currentData)
            return
        }

        viewModelScope.launch {
            setLoading()
            authRepository.sendPasswordResetEmail(emailVal)
                .onSuccess {
                    currentData = currentData.copy(passwordResetSent = true)
                    setSuccess(currentData)
                    onSuccess()
                }
                .onFailure {
                    setError(it.message ?: "Reset request failed")
                }
        }
    }
}
