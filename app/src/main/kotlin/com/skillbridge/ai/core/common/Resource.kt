package com.skillbridge.ai.core.common

/**
 * A generic wrapper that represents a loading state, successful outcome,
 * or failure for data flowing from the data layer to the presentation layer.
 *
 * @param T The type of successful data payload.
 */
sealed class Resource<out T> {

    /** Indicates data is being fetched. */
    data object Loading : Resource<Nothing>()

    /**
     * Indicates a successful operation.
     * @property data The result payload.
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Indicates a failed operation.
     * @property message Human-readable error description.
     * @property throwable Optional underlying exception.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : Resource<Nothing>()
}
