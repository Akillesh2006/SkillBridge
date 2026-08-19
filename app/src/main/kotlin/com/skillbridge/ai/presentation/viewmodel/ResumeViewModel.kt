package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.data.remote.dto.ResumeResponse
import com.skillbridge.ai.domain.repository.ResumeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ResumeUiData(
    val selectedFileName: String? = null,
    val selectedFileBytes: ByteArray? = null,
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val analysisStage: String = "",
    val analysisSuccess: Boolean = false,
    val fileSize: String = "PDF Document",
    val resumeResponse: ResumeResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository
) : BaseViewModel<ResumeUiData>() {

    private var currentData = ResumeUiData()

    init {
        loadResumeData()
    }

    fun loadResumeData() {
        viewModelScope.launch {
            val savedName = resumeRepository.getUploadedResumeName()
            val result = resumeRepository.getResume()

            val resumeResp = result.getOrNull()
            currentData = currentData.copy(
                selectedFileName = savedName ?: resumeResp?.fileName,
                resumeResponse = resumeResp
            )

            setSuccess(currentData)
        }
    }

    fun selectFile(fileName: String, bytes: ByteArray) {
        val sizeFormatted = String.format("%.1f KB", bytes.size / 1024.0)
        currentData = currentData.copy(
            selectedFileName = fileName,
            selectedFileBytes = bytes,
            fileSize = sizeFormatted,
            analysisSuccess = false,
            analysisProgress = 0f,
            analysisStage = "",
            isAnalyzing = false,
            errorMessage = null
        )

        setSuccess(currentData)

        viewModelScope.launch {
            resumeRepository.saveUploadedResumeName(fileName)
        }
    }

    fun startAnalysis(onFinished: () -> Unit) {
        val fileName = currentData.selectedFileName
        val bytes = currentData.selectedFileBytes

        if (fileName.isNullOrBlank() || bytes == null || bytes.isEmpty()) {
            setError("Please select a valid PDF file first")
            return
        }

        viewModelScope.launch {
            currentData = currentData.copy(
                isAnalyzing = true,
                analysisProgress = 0.2f,
                analysisStage = "Uploading Resume to Backend",
                errorMessage = null
            )
            setSuccess(currentData)

            delay(300)
            currentData = currentData.copy(
                analysisProgress = 0.4f,
                analysisStage = "Extracting Text & Analyzing with Gemini AI"
            )
            setSuccess(currentData)

            val result = resumeRepository.uploadResume(fileName, bytes)

            result.onSuccess { response ->
                currentData = currentData.copy(
                    analysisProgress = 0.8f,
                    analysisStage = "Processing AI Career Insights"
                )
                setSuccess(currentData)
                delay(300)

                currentData = currentData.copy(
                    isAnalyzing = false,
                    analysisProgress = 1.0f,
                    analysisStage = "Analysis Complete",
                    analysisSuccess = true,
                    resumeResponse = response
                )
                setSuccess(currentData)
                onFinished()
            }.onFailure { exception ->
                val errorMsg = exception.message ?: "Failed to upload and analyze resume"
                currentData = currentData.copy(
                    isAnalyzing = false,
                    analysisProgress = 0f,
                    analysisStage = "",
                    analysisSuccess = false,
                    errorMessage = errorMsg
                )
                setError(errorMsg)
            }
        }
    }

    fun resetState() {
        currentData = ResumeUiData()
        setSuccess(currentData)
    }
}