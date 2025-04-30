package com.shadowtengu.automl.androidapp.ui.vm // Ensure this package is correct

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Core Library Imports - Double-check package name if necessary
import com.example.automl_prototype_1.model.Dataset
import com.example.automl_prototype_1.model.ExecutionResult
import com.example.automl_prototype_1.service.AppStateService
import com.example.automl_prototype_1.service.ExecutionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File // Required as AppStateService stores a File
import java.lang.Exception // Base Exception class

/**
 * Represents the possible UI states for the Algorithm Selection Screen.
 */
sealed class AlgorithmSelectionState {
    object LoadingDatasetInfo : AlgorithmSelectionState() // Initial state, checking for the File reference
    data class Idle(val datasetFileInfo: String) : AlgorithmSelectionState() // File reference loaded, ready for selection
    object NoDatasetFile : AlgorithmSelectionState() // Navigated here but no valid File reference was found
    object ExecutingAlgorithms : AlgorithmSelectionState() // Algorithms are running via ExecutionService
    data class ExecutionError(val message: String) : AlgorithmSelectionState() // Error during execution
    object ExecutionSuccess : AlgorithmSelectionState() // Execution completed, results stored
}

/**
 * ViewModel for the Algorithm Selection Screen.
 * Manages algorithm selection, retrieves the dataset file from AppStateService,
 * triggers execution via ExecutionService, and updates the UI state.
 */
class AlgorithmSelectionViewModel : ViewModel() {

    // --- Screen State ---
    // Private mutable state holder
    private val _screenState = mutableStateOf<AlgorithmSelectionState>(AlgorithmSelectionState.LoadingDatasetInfo)
    // Public immutable state exposed to the UI
    val screenState: State<AlgorithmSelectionState> = _screenState

    // --- Algorithm Selection ---
    // Tracks the selection state (checked/unchecked) for each available algorithm.
    val availableAlgorithms = mutableStateMapOf(
        "Genetic Algorithm" to false,
        "IGPSO" to false,
        "WWO" to false,
        "BPSO" to false,
        "ASO" to false,
        "NNP" to false
        // Add other algorithms supported by ExecutionService.getAlgorithmImplementation here
    )

    // --- Core Services ---
    private val appStateService = AppStateService.getInstance()
    private val executionService = ExecutionService()

    // --- Dataset File Reference ---
    // Holds the File object retrieved from AppStateService
    private var datasetFileReference: File? = null

    // --- Initialization ---
    // Called when the ViewModel is created. Retrieves the file reference.
    init {
        loadDatasetFileInfoFromAppState()
    }

    /**
     * Retrieves the dataset file reference from AppStateService and updates the screen state.
     */
    private fun loadDatasetFileInfoFromAppState() {
        Log.d("AlgoSelectVM", "Checking AppStateService for dataset file reference...")
        // Get the File object stored by UploadViewModel
        datasetFileReference = appStateService.selectedDatasetFile

        // Check if the File object exists and the file it points to actually exists on disk
        if (datasetFileReference != null && datasetFileReference!!.exists()) {
            Log.i("AlgoSelectVM", "Dataset file found: ${datasetFileReference!!.name}. State set to Idle.")
            // Prepare info string for the UI
            val info = "Using dataset: ${datasetFileReference!!.name}"
            _screenState.value = AlgorithmSelectionState.Idle(info)
        } else {
            // Log an error if the file reference is missing or points to a non-existent file
            Log.e("AlgoSelectVM", "Dataset file reference invalid or file missing. State set to NoDatasetFile.")
            datasetFileReference = null // Ensure local reference is null
            appStateService.selectedDatasetFile = null // Clear invalid entry in shared state too
            _screenState.value = AlgorithmSelectionState.NoDatasetFile
        }
    }

    /**
     * Toggles the selection state for a given algorithm name.
     */
    fun toggleAlgorithmSelection(algorithmName: String) {
        if (availableAlgorithms.containsKey(algorithmName)) {
            val currentState = availableAlgorithms[algorithmName] ?: false
            availableAlgorithms[algorithmName] = !currentState
            Log.d("AlgoSelectVM", "Toggled selection for $algorithmName to ${!currentState}")
        }
    }

    /**
     * Returns a list of names of the currently selected algorithms.
     */
    fun getSelectedAlgorithms(): List<String> {
        // Filters the map for entries where the value (selected state) is true,
        // then extracts the keys (algorithm names).
        return availableAlgorithms.filter { it.value }.keys.toList()
    }

    /**
     * Initiates the execution of the selected algorithms.
     * Loads the Dataset object from the stored file reference, then calls ExecutionService.
     */
    fun runSelectedAlgorithms() {
        val selectedAlgos = getSelectedAlgorithms()
        // Check if at least one algorithm is selected
        if (selectedAlgos.isEmpty()) {
            Log.w("AlgoSelectVM", "Execute requested but no algorithms selected.")
            _screenState.value = AlgorithmSelectionState.ExecutionError("Please select at least one algorithm.")
            // Optional: revert state after delay
            viewModelScope.launch { kotlinx.coroutines.delay(2000); loadDatasetFileInfoFromAppState() }
            return
        }

        // Check if we have a valid File reference
        if (datasetFileReference == null || !datasetFileReference!!.exists()) {
            Log.e("AlgoSelectVM", "Execute requested but dataset file reference is invalid or file missing!")
            _screenState.value = AlgorithmSelectionState.NoDatasetFile
            return
        }

        Log.i("AlgoSelectVM", "Executing algorithms: $selectedAlgos on file ${datasetFileReference!!.name}")
        // Set state to indicate execution is starting
        _screenState.value = AlgorithmSelectionState.ExecutingAlgorithms

        // Launch a coroutine for the potentially long-running operation
        viewModelScope.launch {
            var loadedDataset: Dataset? = null
            var executionResults: List<ExecutionResult>? = null

            // --- Step 1: Load the Dataset object using the File reference ---
            // This happens within the coroutine's background thread context
            try {
                // Use Dispatchers.IO for file loading
                loadedDataset = withContext(Dispatchers.IO) {
                    Log.d("AlgoSelectVM", "Loading Dataset object via executionService.loadData...")
                    // Call the core service method that takes a File
                    executionService.loadData(datasetFileReference!!)
                }
                Log.i("AlgoSelectVM", "Dataset object loaded successfully.")
            } catch (loadEx: Exception) {
                Log.e("AlgoSelectVM", "Failed to load Dataset object from file", loadEx)
                // Update UI state with specific error
                _screenState.value = AlgorithmSelectionState.ExecutionError("Failed to load data: ${loadEx.localizedMessage}")
                return@launch // Exit coroutine if dataset loading fails
            }

            // --- Step 2: Execute Algorithms if Dataset was loaded ---
            if (loadedDataset != null) {
                try {
                    // Use Dispatchers.Default for potentially CPU-intensive algorithms
                    executionResults = withContext(Dispatchers.Default) {
                        Log.d("AlgoSelectVM", "Calling executionService.executeAlgorithms...")
                        // Pass the newly loaded Dataset object
                        executionService.executeAlgorithms(loadedDataset, selectedAlgos, emptyMap())
                    }
                    Log.i("AlgoSelectVM", "Execution completed. Results count: ${executionResults?.size ?: "null"}")

                    // --- Step 3: Store results and update state ---
                    appStateService.executionResults = executionResults // Store results in shared service
                    _screenState.value = AlgorithmSelectionState.ExecutionSuccess // Signal success to UI

                } catch (execEx: Exception) {
                    // Handle errors during algorithm execution
                    Log.e("AlgoSelectVM", "Exception during algorithm execution", execEx)
                    _screenState.value = AlgorithmSelectionState.ExecutionError("Execution failed: ${execEx.localizedMessage}")
                    appStateService.executionResults = null // Clear potentially partial results
                }
            } else {
                // Safety check, should have been caught by loadEx catch block
                Log.e("AlgoSelectVM", "Dataset object was null after loading attempt, cannot execute.")
                _screenState.value = AlgorithmSelectionState.ExecutionError("Internal error: Failed to prepare data.")
                appStateService.executionResults = null
            }
        } // End viewModelScope.launch
    } // End runSelectedAlgorithms

    /**
     * Resets the screen state by re-checking the dataset file reference.
     * Useful for retrying after an error.
     */
    fun returnToIdle() {
        loadDatasetFileInfoFromAppState() // Re-validates file and sets Idle or NoDatasetFile state
    }
}