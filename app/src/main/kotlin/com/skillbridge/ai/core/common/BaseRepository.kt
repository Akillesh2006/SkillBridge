package com.skillbridge.ai.core.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base interface for all repository implementations.
 *
 * Provides a [safeApiCall] utility that executes a network block on [Dispatchers.IO]
 * and wraps the result in a [NetworkResult], handling exceptions uniformly.
 */
interface BaseRepository {

    /**
     * Executes [block] safely on the IO dispatcher.
     *
     * @param block Suspending lambda that performs the network/IO call and returns [T].
     * @return [NetworkResult.Success] wrapping [T] on success, or
     *         [NetworkResult.Exception] wrapping the caught [Throwable] on failure.
     */
    suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(block())
            } catch (e: retrofit2.HttpException) {
                NetworkResult.HttpError(
                    code = e.code(),
                    message = e.message() ?: "HTTP Error",
                )
            } catch (e: Exception) {
                NetworkResult.Exception(e)
            }
        }
    }
}
