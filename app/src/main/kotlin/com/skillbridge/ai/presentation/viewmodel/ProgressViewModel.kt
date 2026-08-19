package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.ProgressMetrics
import com.skillbridge.ai.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : BaseViewModel<ProgressMetrics>() {

    init {
        loadProgressMetrics()
    }

    fun loadProgressMetrics() {
        viewModelScope.launch {
            setLoading()
            try {
                val metrics = progressRepository.getProgressMetrics()
                setSuccess(metrics)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load progress metrics", e)
            }
        }
    }
}
