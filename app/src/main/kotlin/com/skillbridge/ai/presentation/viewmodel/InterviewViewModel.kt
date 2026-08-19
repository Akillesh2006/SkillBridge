package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.data.remote.dto.*
import com.skillbridge.ai.domain.repository.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewUiState(
    val activeInterviewId: Long? = null,
    val role: String = "",
    val questionId: Long? = null,
    val questionText: String = "",
    val questionOrder: Int = 1,
    val difficulty: String = "EASY",
    val isThinking: Boolean = false,
    val completed: Boolean = false,
    val overallScore: Int = 0,
    val feedback: String = "",
    val history: List<InterviewResultResponse> = emptyList(),
    val resultDetails: InterviewResultResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val interviewRepository: InterviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            interviewRepository.getHistory().onSuccess { list ->
                _uiState.update { it.copy(history = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load interview history: ${err.message}") }
            }
        }
    }

    fun startInterview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true, errorMessage = null) }
            interviewRepository.startInterview().onSuccess { start ->
                _uiState.update {
                    it.copy(
                        activeInterviewId = start.interviewId,
                        role = start.role,
                        questionId = start.questionId,
                        questionText = start.questionText,
                        questionOrder = start.questionOrder,
                        difficulty = start.difficulty,
                        completed = start.completed,
                        isThinking = false
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to start interview: ${err.message}", isThinking = false) }
            }
        }
    }

    fun submitAnswer(answerText: String) {
        val id = _uiState.value.activeInterviewId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true, errorMessage = null) }
            interviewRepository.submitAnswer(id, answerText).onSuccess { res ->
                if (res.completed) {
                    _uiState.update {
                        it.copy(
                            completed = true,
                            overallScore = res.overallScore ?: 0,
                            feedback = res.feedback ?: "",
                            isThinking = false
                        )
                    }
                    loadHistory()
                } else {
                    _uiState.update {
                        it.copy(
                            questionId = res.questionId,
                            questionText = res.questionText ?: "",
                            questionOrder = res.questionOrder ?: 1,
                            difficulty = res.difficulty ?: "EASY",
                            isThinking = false
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to submit answer: ${err.message}", isThinking = false) }
            }
        }
    }

    fun loadResultDetails(interviewId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true, errorMessage = null) }
            interviewRepository.getResult(interviewId).onSuccess { res ->
                _uiState.update { it.copy(resultDetails = res, isThinking = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to load interview report: ${err.message}", isThinking = false) }
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
