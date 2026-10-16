package com.skillbridge.ai.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.data.remote.dto.ResumeResponse
import com.skillbridge.ai.domain.repository.ResumeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class ResumeUiData(
    val selectedFileName: String? = null,
    val selectedFileBytes: ByteArray? = null,
    val isPreparing: Boolean = false,
    val preparationStatus: String? = null,
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val analysisStage: String = "",
    val analysisSuccess: Boolean = false,
    val fileSize: String = "PDF Document",
    val resumeResponse: ResumeResponse? = null,
    val errorMessage: String? = null
)

private const val TAG = "RESUME"
private const val MAX_RESUME_SIZE_BYTES = 15 * 1024 * 1024 // 15 MB
private const val READ_TIMEOUT_MS = 30_000L // 30 seconds for Google Drive / cloud documents

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

    /**
     * Process URI returned by ActivityResultLauncher in background Dispatchers.IO.
     * Streams cloud/local document safely with timeout into a temporary cache file.
     */
    fun processSelectedUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            Log.d(TAG, "[RESUME] URI received: $uri")

            currentData = currentData.copy(
                isPreparing = true,
                preparationStatus = "Preparing resume...",
                errorMessage = null
            )
            setSuccess(currentData)

            val result = withContext(Dispatchers.IO) {
                try {
                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(uri) ?: "application/pdf"
                    Log.d(TAG, "[RESUME] MIME type = $mimeType")

                    val fileName = getFileNameFromUri(context, uri) ?: "resume.pdf"
                    Log.d(TAG, "[RESUME] File name = $fileName")

                    val isPdfExt = fileName.lowercase().endsWith(".pdf")
                    val isPdfMime = mimeType.equals("application/pdf", ignoreCase = true) || mimeType.contains("pdf", ignoreCase = true)

                    if (!isPdfExt && !isPdfMime) {
                        return@withContext Result.failure<Pair<String, ByteArray>>(
                            IllegalArgumentException("Invalid file type. Only PDF files (.pdf) are allowed.")
                        )
                    }

                    // Copy stream to cache file with timeout (safely supports Google Drive, Downloads, OneDrive)
                    val cacheFile = File(context.cacheDir, "resume_upload_temp.pdf")
                    val readSuccess = withTimeoutOrNull(READ_TIMEOUT_MS) {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            FileOutputStream(cacheFile).use { outputStream ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var totalBytes = 0L
                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    totalBytes += bytesRead
                                    if (totalBytes > MAX_RESUME_SIZE_BYTES) {
                                        throw IllegalArgumentException("Resume file is too large (max 15MB).")
                                    }
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                                outputStream.flush()
                            }
                        }
                        true
                    }

                    if (readSuccess == null) {
                        cacheFile.delete()
                        return@withContext Result.failure<Pair<String, ByteArray>>(
                            IllegalStateException("Document provider took too long to stream the file. Please try downloading it locally or select again.")
                        )
                    }

                    val fileLength = cacheFile.length()
                    Log.d(TAG, "[RESUME] Temporary file created: ${cacheFile.absolutePath}")
                    Log.d(TAG, "[RESUME] File size = $fileLength bytes")

                    if (fileLength <= 0) {
                        cacheFile.delete()
                        return@withContext Result.failure<Pair<String, ByteArray>>(
                            IllegalArgumentException("Selected PDF file is empty (0 bytes).")
                        )
                    }

                    val bytes = cacheFile.readBytes()
                    Result.success(Pair(fileName, bytes))
                } catch (e: Exception) {
                    Log.e(TAG, "[RESUME] Failed to process selected URI", e)
                    Result.failure(e)
                }
            }

            result.onSuccess { (fileName, bytes) ->
                val sizeFormatted = String.format("%.1f KB", bytes.size / 1024.0)
                currentData = currentData.copy(
                    selectedFileName = fileName,
                    selectedFileBytes = bytes,
                    fileSize = sizeFormatted,
                    isPreparing = false,
                    preparationStatus = null,
                    analysisSuccess = false,
                    analysisProgress = 0f,
                    analysisStage = "",
                    isAnalyzing = false,
                    errorMessage = null
                )
                setSuccess(currentData)

                resumeRepository.saveUploadedResumeName(fileName)
            }.onFailure { exception ->
                val errorMsg = exception.message ?: "Failed to read selected resume file."
                currentData = currentData.copy(
                    isPreparing = false,
                    preparationStatus = null,
                    errorMessage = errorMsg
                )
                setSuccess(currentData)
            }
        }
    }

    /**
     * Handled when user cancels the document picker or presses back
     */
    fun onPickerCancelled() {
        Log.d(TAG, "[RESUME] Picker cancelled by user")
        currentData = currentData.copy(
            isPreparing = false,
            preparationStatus = null
        )
        setSuccess(currentData)
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
            currentData = currentData.copy(errorMessage = "Please select a valid PDF file first.")
            setSuccess(currentData)
            return
        }

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "[RESUME] Upload started for $fileName (${bytes.size} bytes)")

            currentData = currentData.copy(
                isAnalyzing = true,
                analysisProgress = 0.35f,
                analysisStage = "Uploading resume to server...",
                errorMessage = null
            )
            setSuccess(currentData)

            val result = resumeRepository.uploadResume(fileName, bytes)

            result.onSuccess { response ->
                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "[RESUME] Upload completed and AI analysis finished in $duration ms")
                Log.d(TAG, "[RESUME] Analysis completed successfully")

                currentData = currentData.copy(
                    isAnalyzing = false,
                    analysisProgress = 1.0f,
                    analysisStage = "Analysis Complete",
                    analysisSuccess = true,
                    resumeResponse = response,
                    errorMessage = null
                )
                setSuccess(currentData)
                onFinished()
            }.onFailure { exception ->
                val errorMsg = exception.message ?: "Failed to upload and analyze resume."
                Log.e(TAG, "[RESUME] Upload failed: $errorMsg", exception)
                currentData = currentData.copy(
                    isAnalyzing = false,
                    analysisProgress = 0f,
                    analysisStage = "",
                    analysisSuccess = false,
                    errorMessage = errorMsg
                )
                setSuccess(currentData)
            }
        }
    }

    fun clearError() {
        currentData = currentData.copy(errorMessage = null)
        setSuccess(currentData)
    }

    fun resetState() {
        currentData = ResumeUiData()
        setSuccess(currentData)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query display name from cursor", e)
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}