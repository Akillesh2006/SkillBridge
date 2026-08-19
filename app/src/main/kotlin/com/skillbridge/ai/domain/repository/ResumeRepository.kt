package com.skillbridge.ai.domain.repository

import com.skillbridge.ai.data.remote.dto.ResumeResponse

/**
 * Resume repository interface for handling CV uploading,
 * parsing, and retrieving resume information.
 */
interface ResumeRepository : BaseRepositoryInterface {

    suspend fun uploadResume(
        fileName: String,
        fileBytes: ByteArray
    ): Result<ResumeResponse>

    suspend fun getResume(): Result<ResumeResponse>

    suspend fun getUploadedResumeName(): String?

    suspend fun saveUploadedResumeName(fileName: String)
}