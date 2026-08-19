package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.skillbridge.ai.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Abstract base ViewModel providing a typed [UiState] flow.
 *
 * Concrete ViewModels should extend this class and:
 * 1. Override [T] with their screen-specific data type.
 * 2. Call [setLoading], [setSuccess], [setError], or [setEmpty] to drive the UI.
 *
 * @param T The type of the data payload used in [UiState.Success].
 */
abstract class BaseViewModel<T> : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)

    /** Observed by the Compose screen to render the correct UI state. */
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    /** Transitions the UI to the loading state. */
    protected fun setLoading() {
        _uiState.value = UiState.Loading
    }

    /**
     * Transitions the UI to the success state with [data].
     * @param data The successfully loaded content.
     */
    protected fun setSuccess(data: T) {
        _uiState.value = UiState.Success(data)
    }

    /**
     * Transitions the UI to the error state.
     * @param message Human-readable error description.
     * @param throwable Optional underlying exception.
     */
    protected fun setError(message: String, throwable: Throwable? = null) {
        _uiState.value = UiState.Error(message, throwable)
    }

    /** Transitions the UI to the empty state (no data to display). */
    protected fun setEmpty() {
        _uiState.value = UiState.Empty
    }
}
