package com.shadowtengu.automl.androidapp.ui.screens // Verify this package matches the file location

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// Import navigation destinations and the ViewModel/State
import com.shadowtengu.automl.androidapp.ui.navigation.AppDestinations
import com.shadowtengu.automl.androidapp.ui.vm.AlgorithmSelectionViewModel
import com.shadowtengu.automl.androidapp.ui.vm.AlgorithmSelectionState

/**
 * Composable function for the Algorithm Selection Screen.
 * Displays the loaded dataset info, allows users to select algorithms,
 * and triggers algorithm execution via the ViewModel.
 *
 * @param navController The NavController for handling navigation events.
 * @param viewModel The ViewModel instance for this screen.
 */
@Composable
fun AlgorithmSelectionScreen(
    navController: NavController,
    viewModel: AlgorithmSelectionViewModel = viewModel() // Obtain ViewModel instance
) {
    // Log to confirm this specific composable is being executed
    Log.d("WHICH_SCREEN", "--- Rendering AlgorithmSelectionScreen ---")

    // Observe the screen state from the ViewModel
    val screenState by viewModel.screenState // Use 'by' delegate for cleaner access
    // Observe the map containing algorithm names and their selected status (true/false)
    // We use remember because the map itself is mutable state within the VM
    val algorithmSelectionMap = remember { viewModel.availableAlgorithms }

    // --- LaunchedEffect for Navigation on ExecutionSuccess ---
    // This effect runs when the composable enters the composition and whenever
    // the key (screenState) changes. It handles navigation automatically.
    LaunchedEffect(key1 = screenState) {
        if (screenState is AlgorithmSelectionState.ExecutionSuccess) {
            Log.i("AlgoSelectScreen", "Execution successful state detected, navigating to results.")
            // Navigate to the Results screen
            navController.navigate(AppDestinations.RESULTS_ROUTE) {
                // Optional: Remove AlgorithmSelectionScreen from back stack
                // popUpTo(AppDestinations.ALGORITHM_SELECTION_ROUTE) { inclusive = true }
            }
            // Optional: Notify ViewModel that navigation occurred if state needs reset
            // viewModel.navigationCompleted()
        }
    }
    // --- End LaunchedEffect ---

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize() // Occupy all screen space
            .padding(16.dp), // Apply padding
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        Text("Select Algorithms", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Dynamically Display Content Based on State ---
        when (val state = screenState) { // Use 'state' variable for easier access inside when

            is AlgorithmSelectionState.LoadingDatasetInfo -> {
                // Show loading indicator while ViewModel checks AppStateService
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Checking for loaded dataset...")
            }

            is AlgorithmSelectionState.NoDatasetFile -> {
                // Show error if no valid dataset file was found
                Text(
                    "Error: No dataset loaded or file is missing.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) { // Button to go back
                    Text("Go Back to Upload")
                }
            }

            is AlgorithmSelectionState.Idle -> {
                // Display info about the loaded dataset file
                Text(
                    text = state.datasetFileInfo, // Info comes from ViewModel state
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // --- Algorithm List (Scrollable) ---
                Text("Available Algorithms:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f) // Allows list to scroll and take available vertical space
                ) {
                    // Create a row for each algorithm in the ViewModel's map
                    items(items = algorithmSelectionMap.keys.toList(), key = { it }) { algoName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                // Checked state comes from the map in the ViewModel
                                checked = algorithmSelectionMap[algoName] ?: false,
                                // When checked/unchecked, notify the ViewModel
                                onCheckedChange = { viewModel.toggleAlgorithmSelection(algoName) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Display the algorithm name
                            Text(algoName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- Run Button ---
                Button(
                    onClick = { viewModel.runSelectedAlgorithms() },
                    // Enable the button only if at least one algorithm is selected
                    enabled = viewModel.getSelectedAlgorithms().isNotEmpty()
                ) {
                    Text("Run Selected Algorithms")
                }
            } // End Idle State

            is AlgorithmSelectionState.ExecutingAlgorithms -> {
                // Show loading indicator and text during execution
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Executing selected algorithms...")
            }

            is AlgorithmSelectionState.ExecutionError -> {
                // Display error message if execution fails
                Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Button to go back to the selection state
                Button(onClick = { viewModel.returnToIdle() }) {
                    Text("Try Again")
                }
            }

            is AlgorithmSelectionState.ExecutionSuccess -> {
                // This state is primarily handled by LaunchedEffect for navigation.
                // Optionally display a brief success message before navigation occurs.
                Text("Execution Complete!", color = MaterialTheme.colorScheme.primary)
                // Could add a slight delay here if needed before navigation, but LaunchedEffect handles it.
            }
        } // End when block
    } // End Column
}