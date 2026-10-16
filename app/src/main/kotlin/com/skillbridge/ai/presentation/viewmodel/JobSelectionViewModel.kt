package com.skillbridge.ai.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.skillbridge.ai.domain.repository.JobRepository
import com.skillbridge.ai.domain.repository.ProfileRepository
import com.skillbridge.ai.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobSelectionUiData(
    val popularJobs: List<String> = emptyList(),
    val popularCompanies: List<String> = emptyList(),
    val searchResults: List<String> = emptyList(),
    val selectedJobTitle: String = "",
    val selectedCompany: String = "",
    val jobSummary: String = "",
    val isSavedSuccessfully: Boolean = false,
    val initialJobTitle: String = "",
    val initialCompany: String = ""
)

@HiltViewModel
class JobSelectionViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository,
    private val roadmapRepository: RoadmapRepository
) : BaseViewModel<JobSelectionUiData>() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _companyQuery = MutableStateFlow("")
    val companyQuery: StateFlow<String> = _companyQuery.asStateFlow()

    private var currentData = JobSelectionUiData()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            setLoading()
            try {
                val popJobs = jobRepository.getPopularJobs()
                val popComps = jobRepository.getPopularCompanies()
                val savedJob = jobRepository.getSavedTargetJob()

                val initialJob = savedJob?.first ?: "Android Developer"
                val initialComp = savedJob?.second ?: "Any company"

                currentData = JobSelectionUiData(
                    popularJobs = popJobs,
                    popularCompanies = popComps,
                    searchResults = popJobs,
                    selectedJobTitle = initialJob,
                    selectedCompany = initialComp,
                    jobSummary = getMockSummaryFor(initialJob, initialComp),
                    initialJobTitle = initialJob,
                    initialCompany = initialComp
                )
                setSuccess(currentData)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load roles data", e)
            }
        }
    }

    fun searchRoles(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val results = jobRepository.searchJobs(query)
            currentData = currentData.copy(searchResults = results)
            setSuccess(currentData)
        }
    }

    fun searchCompanies(query: String) {
        _companyQuery.value = query
    }

    fun selectJobTitle(title: String) {
        currentData = currentData.copy(
            selectedJobTitle = title,
            jobSummary = getMockSummaryFor(title, currentData.selectedCompany),
            isSavedSuccessfully = false
        )
        setSuccess(currentData)
    }

    fun selectCompany(company: String) {
        currentData = currentData.copy(
            selectedCompany = company,
            jobSummary = getMockSummaryFor(currentData.selectedJobTitle, company),
            isSavedSuccessfully = false
        )
        setSuccess(currentData)
    }

    fun saveTargetJobSelection() {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            setLoading()
            jobRepository.saveTargetJob(currentData.selectedJobTitle, currentData.selectedCompany)
                .onSuccess {
                    val duration = System.currentTimeMillis() - start
                    android.util.Log.d("SkillBridgePerf", "[PERF] TargetJob update = $duration ms")
                    currentData = currentData.copy(
                        isSavedSuccessfully = true,
                        initialJobTitle = currentData.selectedJobTitle,
                        initialCompany = currentData.selectedCompany
                    )
                    setSuccess(currentData)
                }
                .onFailure {
                    setError(it.message ?: "Failed to save job")
                }
        }
    }

    fun resetSavedState() {
        currentData = currentData.copy(isSavedSuccessfully = false)
        setSuccess(currentData)
    }

    private fun getMockSummaryFor(job: String, company: String): String {
        val targetComp = if (company.isBlank() || company.equals("Any company", ignoreCase = true)) "the tech industry" else company
        return "Target Career Profile: $job at $targetComp. Learning paths, roadmaps, and mock interview preparations will be dynamically generated targeting commonly expected areas for this career profile."
    }
}
