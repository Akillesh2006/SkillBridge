package com.skillbridge.ai.core.common

/**
 * Represents the complete lifecycle of a UI screen's data state.
 *
 * Used by ViewModels to expose state via [kotlinx.coroutines.flow.StateFlow].
 *
 * @param T The type of data when in [Success] state.
 */
sealed class UiState<out T> {

    /** The screen is actively loading data. Show a progress indicator. */
    data object Loading : UiState<Nothing>()

    /**
     * Data loaded successfully.
     * @property data The content to display.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * An error occurred while loading or processing data.
     * @property message User-facing error description.
     * @property throwable Optional underlying exception for logging.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : UiState<Nothing>()

    /** No data is available to display (e.g. empty search results). */
    data object Empty : UiState<Nothing>()
}

/** Returns `true` if this state represents an active loading operation. */
val UiState<*>.isLoading: Boolean get() = this is UiState.Loading

/** Returns the data if this state is [UiState.Success], otherwise `null`. */
fun <T> UiState<T>.dataOrNull(): T? = (this as? UiState.Success)?.data
