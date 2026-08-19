package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.Assessment
import com.skillbridge.ai.domain.repository.AssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class MasteryCheckUiData(
    val assessment: Assessment? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOption: Int? = null,
    val answers: Map<Long, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MasteryCheckViewModel @Inject constructor(
    private val assessmentRepository: AssessmentRepository
) : BaseViewModel<MasteryCheckUiData>() {

    private var currentData = MasteryCheckUiData()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    init {
        setSuccess(currentData)
    }

    fun startMasteryCheck(taskId: Long) {
        viewModelScope.launch {
            setLoading()
            assessmentRepository.startMasteryCheck(taskId)
                .onSuccess { newAssessment ->
                    currentData = currentData.copy(
                        assessment = newAssessment,
                        currentQuestionIndex = 0,
                        selectedOption = null,
                        answers = emptyMap(),
                        error = null
                    )
                    setSuccess(currentData)
                }
                .onFailure {
                    setError(it.message ?: "Failed to generate mastery check")
                    currentData = currentData.copy(assessment = null, error = it.message)
                    setSuccess(currentData)
                    _errorFlow.emit(it.message ?: "Failed to start mastery check")
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

    fun submitMasteryCheck(onCompleted: (Long) -> Unit) {
        val assessment = currentData.assessment ?: return
        if (currentData.isSubmitting) return

        viewModelScope.launch {
            currentData = currentData.copy(isSubmitting = true)
            setSuccess(currentData)

            val answersList = currentData.answers.map { Pair(it.key, it.value) }
            
            assessmentRepository.submitMasteryCheck(assessment.id, answersList)
                .onSuccess { gradedAssessment ->
                    currentData = currentData.copy(
                        assessment = gradedAssessment,
                        isSubmitting = false
                    )
                    setSuccess(currentData)
                    onCompleted(gradedAssessment.id)
                }
                .onFailure {
                    currentData = currentData.copy(isSubmitting = false)
                    setSuccess(currentData)
                    _errorFlow.emit(it.message ?: "Failed to submit answers")
                }
        }
    }

    fun loadMasteryCheckResult(assessmentId: Long) {
        viewModelScope.launch {
            setLoading()
            assessmentRepository.getMasteryCheckResult(assessmentId)
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

    fun retestMasteryCheck(assessmentId: Long) {
        viewModelScope.launch {
            setLoading()
            assessmentRepository.retestMasteryCheck(assessmentId)
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
                    setError(it.message ?: "Failed to retest mastery check")
                    _errorFlow.emit(it.message ?: "Failed to start retest")
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
