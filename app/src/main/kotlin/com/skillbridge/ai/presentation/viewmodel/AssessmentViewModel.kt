package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.Assessment
import com.skillbridge.ai.domain.model.AssessmentHistoryItem
import com.skillbridge.ai.domain.repository.AssessmentRepository
import com.skillbridge.ai.core.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class AssessmentUiData(
    val assessment: Assessment? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOption: Int? = null,
    val answers: Map<Long, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val history: List<AssessmentHistoryItem> = emptyList()
)

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val assessmentRepository: AssessmentRepository
) : BaseViewModel<AssessmentUiData>() {

    private var currentData = AssessmentUiData()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    init {
        setSuccess(currentData)
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            assessmentRepository.getAssessmentHistory()
                .onSuccess { list ->
                    currentData = currentData.copy(history = list)
                    setSuccess(currentData)
                }
                .onFailure {
                    _errorFlow.emit(it.message ?: "Failed to load history")
                }
        }
    }

    fun startTest() {
        viewModelScope.launch {
            setLoading()
            assessmentRepository.startAssessment()
                .onSuccess { newAssessment ->
                    currentData = currentData.copy(
                        assessment = newAssessment,
                        currentQuestionIndex = 0,
                        selectedOption = null,
                        answers = emptyMap()
                    )
                    setSuccess(currentData)
                }
                .onFailure {
                    setError(it.message ?: "Failed to generate skill assessment test")
                    currentData = currentData.copy(assessment = null)
                    setSuccess(currentData)
                    _errorFlow.emit(it.message ?: "Failed to generate assessment")
                }
        }
    }

    fun selectOption(questionId: Long, optionIndex: Int) {
        val updatedAnswers = currentData.answers.toMutableMap().apply {
            put(questionId, optionIndex)
        }
        currentData = currentData.copy(
            selectedOption = optionIndex,
            answers = updatedAnswers
        )
        setSuccess(currentData)
    }

    fun nextQuestion() {
        val questions = currentData.assessment?.questions ?: return
        if (currentData.currentQuestionIndex < questions.size - 1) {
            val nextIndex = currentData.currentQuestionIndex + 1
            val nextQuestionId = questions[nextIndex].id
            val prevSelected = currentData.answers[nextQuestionId]
            
            currentData = currentData.copy(
                currentQuestionIndex = nextIndex,
                selectedOption = prevSelected
            )
            setSuccess(currentData)
        }
    }

    fun previousQuestion() {
        if (currentData.currentQuestionIndex > 0) {
            val prevIndex = currentData.currentQuestionIndex - 1
            val questions = currentData.assessment?.questions ?: return
            val prevQuestionId = questions[prevIndex].id
            val prevSelected = currentData.answers[prevQuestionId]

            currentData = currentData.copy(
                currentQuestionIndex = prevIndex,
                selectedOption = prevSelected
            )
            setSuccess(currentData)
        }
    }

    fun submitTest(onCompleted: (Long) -> Unit) {
        val assessment = currentData.assessment ?: return
        if (currentData.isSubmitting) return

        viewModelScope.launch {
            currentData = currentData.copy(isSubmitting = true)
            setSuccess(currentData)

            val answersList = currentData.answers.map { Pair(it.key, it.value) }
            
            assessmentRepository.submitAssessment(assessment.id, answersList)
                .onSuccess { gradedAssessment ->
                    currentData = currentData.copy(
                        assessment = gradedAssessment,
                        isSubmitting = false
                    )
                    setSuccess(currentData)
                    loadHistory() // Reload history with the new result
                    onCompleted(gradedAssessment.id)
                }
                .onFailure {
                    currentData = currentData.copy(isSubmitting = false)
                    setSuccess(currentData)
                    _errorFlow.emit(it.message ?: "Failed to submit answers")
                }
        }
    }

    fun loadAssessmentResult(assessmentId: Long) {
        viewModelScope.launch {
            setLoading()
            assessmentRepository.getAssessmentResult(assessmentId)
                .onSuccess { loadedResult ->
                    currentData = currentData.copy(assessment = loadedResult)
                    setSuccess(currentData)
                }
                .onFailure {
                    setError(it.message ?: "Failed to load result details")
                    currentData = currentData.copy(assessment = null)
                    setSuccess(currentData)
                }
        }
    }

    fun clearActiveAssessment() {
        currentData = currentData.copy(
            assessment = null,
            currentQuestionIndex = 0,
            selectedOption = null,
            answers = emptyMap()
        )
        setSuccess(currentData)
    }
}
