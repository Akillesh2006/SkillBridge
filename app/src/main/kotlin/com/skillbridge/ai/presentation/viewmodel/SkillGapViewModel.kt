package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.model.SkillGapAnalysis
import com.skillbridge.ai.domain.repository.SkillGapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SkillGapViewModel @Inject constructor(
    private val skillGapRepository: SkillGapRepository
) : BaseViewModel<SkillGapAnalysis>() {

    init {
        loadSkillGapAnalysis()
    }

    fun loadSkillGapAnalysis() {
        viewModelScope.launch {
            setLoading()
            try {
                val analysis = skillGapRepository.getSkillGapAnalysis()
                setSuccess(analysis)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load skill gap analysis", e)
            }
        }
    }
}
