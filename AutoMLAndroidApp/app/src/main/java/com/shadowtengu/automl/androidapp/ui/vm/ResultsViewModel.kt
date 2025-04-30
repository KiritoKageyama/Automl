package com.shadowtengu.automl.androidapp.ui.vm

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.automl_prototype_1.model.ExecutionResult // Import core model
import com.example.automl_prototype_1.service.AppStateService
import android.util.Log

/**
 * Represents the state for the Results Screen.
 */
sealed class ResultsScreenState {
    object Loading : ResultsScreenState() // Initial state while fetching results
    data class Success(val results: List<ExecutionResult>) : ResultsScreenState() // Results loaded
    object NoResults : ResultsScreenState() // Navigated here, but no results found in AppStateService
    data class Error(val message: String) : ResultsScreenState() // Error state (optional)
}

/**
 * ViewModel for the Results Screen.
 * Fetches the execution results stored in AppStateService.
 */
class ResultsViewModel : ViewModel() {

    private val _screenState = mutableStateOf<ResultsScreenState>(ResultsScreenState.Loading)
    val screenState: State<ResultsScreenState> = _screenState

    private val appStateService = AppStateService.getInstance()

    init {
        loadResults()
    }

    /**
     * Fetches the execution results from the AppStateService and updates the UI state.
     */
    private fun loadResults() {
        Log.d("ResultsViewModel", "Loading execution results from AppStateService...")
        val results = appStateService.executionResults // Get the results list

        if (results != null && results.isNotEmpty()) {
            Log.i("ResultsViewModel", "Found ${results.size} results.")
            _screenState.value = ResultsScreenState.Success(results)
        } else {
            Log.w("ResultsViewModel", "No execution results found in AppStateService.")
            _screenState.value = ResultsScreenState.NoResults
        }
        // Optional: Clear results from AppStateService after loading them here?
        // Depends if you want them to persist across app restarts without rerunning.
        // appStateService.executionResults = null
    }

    /**
     * Can be called if the user wants to manually refresh or retry loading.
     */
    fun refreshResults() {
        _screenState.value = ResultsScreenState.Loading
        loadResults()
    }
}