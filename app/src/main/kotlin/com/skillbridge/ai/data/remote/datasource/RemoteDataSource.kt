package com.skillbridge.ai.data.remote.datasource

import com.skillbridge.ai.core.common.NetworkResult
import com.skillbridge.ai.data.remote.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Abstract base class for remote data sources.
 *
 * Provides [safeApiCall] which executes a Retrofit suspend call on [Dispatchers.IO]
 * and wraps the result in a [NetworkResult], handling [retrofit2.HttpException] and
 * generic [Exception] uniformly.
 *
 * Concrete data sources should inject [ApiService] and delegate calls through
 * [safeApiCall] to ensure consistent error handling.
 */
abstract class RemoteDataSource {

    /**
     * Executes [apiCall] on the IO dispatcher and wraps the result.
     *
     * @param apiCall Suspending lambda that performs the Retrofit call.
     * @return [NetworkResult.Success] on success, [NetworkResult.HttpError] for
     *         4xx/5xx responses, or [NetworkResult.Exception] for other failures.
     */
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(apiCall())
            } catch (e: retrofit2.HttpException) {
                Timber.w(e, "HTTP error: ${e.code()}")
                NetworkResult.HttpError(
                    code = e.code(),
                    message = e.message() ?: "HTTP Error",
                )
            } catch (e: Exception) {
                Timber.e(e, "Network call failed")
                NetworkResult.Exception(e)
            }
        }
    }
}
