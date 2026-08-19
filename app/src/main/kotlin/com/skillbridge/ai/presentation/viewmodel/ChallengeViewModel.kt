package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeUiState(
    val activeChallenge: ChallengeGenerateResponse? = null,
    val isThinking: Boolean = false,
    val resultDetails: ChallengeResultResponse? = null,
    val history: List<ChallengeResultResponse> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            challengeRepository.getHistory().onSuccess { list ->
                _uiState.update { it.copy(history = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load challenge history: ${err.message}") }
            }
        }
    }

    fun generateChallenge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true, errorMessage = null, resultDetails = null) }
            challengeRepository.generateChallenge().onSuccess { ch ->
                _uiState.update { it.copy(activeChallenge = ch, isThinking = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to generate challenge: ${err.message}", isThinking = false) }
            }
        }
    }

    fun submitAnswer(answer: String) {
        val ch = _uiState.value.activeChallenge ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true, errorMessage = null) }
            challengeRepository.submitChallengeAnswer(ch.challengeId, answer).onSuccess { res ->
                _uiState.update {
                    it.copy(
                        resultDetails = res,
                        activeChallenge = null,
                        isThinking = false
                    )
                }
                loadHistory()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to submit answer: ${err.message}", isThinking = false) }
            }
        }
    }

    fun clearResultDetails() {
        _uiState.update { it.copy(resultDetails = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
