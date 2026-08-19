package com.skillbridge.ai.data.repository

import com.skillbridge.ai.data.remote.api.ApiService
import com.skillbridge.ai.data.remote.dto.ResumeResponse
import com.skillbridge.ai.domain.repository.ResumeRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ResumeRepository {

    private var uploadedFileName: String? = null

    override suspend fun uploadResume(
        fileName: String,
        fileBytes: ByteArray
    ): Result<ResumeResponse> {

        return try {

            if (fileBytes.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException("Resume file is empty (0 bytes)")
                )
            }

            android.util.Log.d("ResumeUpload", "Preparing Multipart Request")
            android.util.Log.d("ResumeUpload", "Upload File Name: $fileName")
            android.util.Log.d("ResumeUpload", "File Bytes Length: ${fileBytes.size}")

            val requestBody = fileBytes.toRequestBody(
                "application/pdf".toMediaType()
            )

            val multipartFile = MultipartBody.Part.createFormData(
                "file",
                fileName.replace(" ", "_"),
                requestBody
            )

            val response = apiService.uploadResume(multipartFile)

            uploadedFileName = response.fileName
            android.util.Log.d("ResumeUpload", "Upload Success! Response ID: ${response.id}, File: ${response.fileName}")

            Result.success(response)

        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val rawErrorBody = e.response()?.errorBody()?.string()?.trim()
            android.util.Log.e("ResumeUpload", "HTTP Error $code: $rawErrorBody", e)

            val userMsg = if (!rawErrorBody.isNullOrBlank() && !rawErrorBody.startsWith("<!DOCTYPE") && !rawErrorBody.startsWith("<html>")) {
                rawErrorBody
            } else {
                when (code) {
                    400 -> "Invalid resume file. Please select a valid PDF."
                    401 -> "Authentication expired. Please log in again."
                    500 -> "Server error during PDF analysis. Please try again."
                    else -> "Upload error ($code): Failed to process PDF."
                }
            }
            Result.failure(Exception(userMsg))
        } catch (e: Exception) {
            android.util.Log.e("ResumeUpload", "Network or upload exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get the currently uploaded resume and its extracted information
     * from the Spring Boot backend.
     */
    override suspend fun getResume(): Result<ResumeResponse> {

        return try {

            val response = apiService.getResume()

            uploadedFileName = response.fileName

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUploadedResumeName(): String? {
        return uploadedFileName
    }

    override suspend fun saveUploadedResumeName(fileName: String) {
        uploadedFileName = fileName
    }
}