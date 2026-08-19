package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.CopilotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CopilotUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Welcome to your AI Career Copilot 🚀\n\nI have loaded your profile context. How can I help you accelerate your learning today?",
            isUser = false
        )
    ),
    val isThinking: Boolean = false,
    val copilotContext: CopilotContextResponse? = null,
    val dailyMission: DailyMissionResponse? = null,
    val readiness: CareerReadinessResponse? = null,
    val settings: ReminderSettingsRequest? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CopilotViewModel @Inject constructor(
    private val copilotRepository: CopilotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CopilotUiState())
    val uiState: StateFlow<CopilotUiState> = _uiState.asStateFlow()

    init {
        loadContext()
        loadDailyMission()
    }

    fun loadContext() {
        viewModelScope.launch {
            copilotRepository.getCopilotContext().onSuccess { context ->
                _uiState.update { it.copy(copilotContext = context) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load context: ${err.message}") }
            }
        }
    }

    fun loadDailyMission() {
        viewModelScope.launch {
            copilotRepository.getDailyMission().onSuccess { mission ->
                _uiState.update { it.copy(dailyMission = mission) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load daily mission: ${err.message}") }
            }
        }
    }

    fun loadCareerReadiness() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            copilotRepository.getCareerReadiness().onSuccess { r ->
                _uiState.update { it.copy(readiness = r, isThinking = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load readiness report: ${err.message}", isThinking = false) }
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            copilotRepository.getReminderSettings().onSuccess { s ->
                _uiState.update { it.copy(settings = s) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load settings: ${err.message}") }
            }
        }
    }

    fun updateSettings(settings: ReminderSettingsRequest) {
        viewModelScope.launch {
            copilotRepository.updateReminderSettings(settings).onSuccess {
                _uiState.update { it.copy(settings = settings, errorMessage = "Settings saved successfully.") }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to save settings: ${err.message}") }
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isThinking) return

        val userMessage = ChatMessage(text = trimmed, isUser = true)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isThinking = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            copilotRepository.chatCopilot(trimmed).onSuccess { reply ->
                val aiMessage = ChatMessage(text = reply.message, isUser = false)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + aiMessage,
                        isThinking = false
                    )
                }
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        isThinking = false,
                        errorMessage = err.message ?: "Copilot service is temporarily unavailable."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
