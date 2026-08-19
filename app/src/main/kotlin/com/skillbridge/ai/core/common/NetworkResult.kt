package com.skillbridge.ai.core.common

/**
 * A network-specific result wrapper used in the remote data source layer.
 *
 * Separates HTTP-level errors from application-level errors and from
 * non-network exceptions (e.g. JSON parse failures, IO errors).
 *
 * @param T The type of successful response body.
 */
sealed class NetworkResult<out T> {

    /**
     * Successful HTTP response with a parsed body.
     * @property data Parsed response data.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * HTTP error response (4xx / 5xx).
     * @property code HTTP status code.
     * @property message Server-provided error message.
     */
    data class HttpError(
        val code: Int,
        val message: String,
    ) : NetworkResult<Nothing>()

    /**
     * Non-HTTP failure such as a network timeout, no connectivity,
     * or a serialisation exception.
     * @property throwable The underlying exception.
     */
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
}

/** Maps a [NetworkResult] to a [Resource] for use in the domain/presentation layer. */
fun <T> NetworkResult<T>.toResource(): Resource<T> = when (this) {
    is NetworkResult.Success -> Resource.Success(data)
    is NetworkResult.HttpError -> {
        val userFriendlyMessage = when (code) {
            401 -> "Session expired or unauthorized. Please log in again."
            403 -> "Access denied. You do not have permission for this action."
            404 -> "The requested service endpoint was not found."
            in 500..599 -> "Cloud service is currently unavailable. Please try again in a few moments."
            else -> if (message.isNotBlank()) message else "Server request failed with code $code."
        }
        Resource.Error(userFriendlyMessage)
    }
    is NetworkResult.Exception -> {
        val userFriendlyMessage = when (throwable) {
            is java.net.UnknownHostException, is java.net.ConnectException ->
                "Unable to connect to SkillBridge server. Please check your internet connection or backend deployment status."
            is java.net.SocketTimeoutException ->
                "Connection timed out. The cloud server took too long to respond."
            is javax.net.ssl.SSLException ->
                "Secure connection failure. Please verify HTTPS configuration."
            else -> throwable.localizedMessage ?: "A network error occurred. Please try again."
        }
        Resource.Error(userFriendlyMessage, throwable)
    }
}
