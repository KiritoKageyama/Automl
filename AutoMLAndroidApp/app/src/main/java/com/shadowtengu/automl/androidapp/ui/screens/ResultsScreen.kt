package com.shadowtengu.automl.androidapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.automl_prototype_1.model.ExecutionResult // Import core model
import com.shadowtengu.automl.androidapp.ui.vm.ResultsViewModel
import com.shadowtengu.automl.androidapp.ui.vm.ResultsScreenState // Import state
import java.util.Locale // For formatting doubles

/**
 * Composable function for the Results Screen.
 * Displays the results of the executed algorithms.
 *
 * @param navController The NavController (potentially unused here, but good practice to pass).
 * @param viewModel The ViewModel for this screen.
 */
@Composable
fun ResultsScreen(
    navController: NavController, // May not be needed if no further navigation from here
    viewModel: ResultsViewModel = viewModel()
) {
    Log.d("WHICH_SCREEN", "--- Rendering ResultsScreen ---")
    val screenState by viewModel.screenState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Execution Results", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = screenState) {
            is ResultsScreenState.Loading -> {
                CircularProgressIndicator()
                Text("Loading results...")
            }
            is ResultsScreenState.NoResults -> {
                Text("No results found.", style = MaterialTheme.typography.bodyLarge)
                // Optionally add a button to go back or refresh
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() /* Or navigate somewhere else */ }) {
                    Text("Go Back")
                }
            }
            is ResultsScreenState.Error -> {
                Text(
                    "Error loading results: ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.refreshResults() }) {
                    Text("Retry")
                }
            }
            is ResultsScreenState.Success -> {
                // Display the results in a scrollable list
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    // Header Row (Optional but good UI)
                    item {
                        Row(Modifier.padding(bottom = 8.dp)) {
                            TableCell("Algorithm", weight = 3f, title = true)
                            TableCell("Accuracy", weight = 2f, title = true)
                            TableCell("AUC", weight = 2f, title = true)
                            TableCell("Loss", weight = 2f, title = true)
                            TableCell("Time (ms)", weight = 2f, title = true)
                        }
                        Divider()
                    }

                    // Data Rows
                    items(state.results) { result ->
                        ResultRow(result = result)
                        Divider()
                    }
                }
            } // End Success State
        } // End when
    } // End Column
}

/**
 * Composable for displaying a single row in the results table.
 */
@Composable
private fun ResultRow(result: ExecutionResult) {
    Row(Modifier.padding(vertical = 8.dp)) {
        TableCell(result.algorithmName, weight = 3f)
        // Format doubles to a reasonable number of decimal places
        TableCell(String.format(Locale.US, "%.4f", result.accuracy), weight = 2f)
        TableCell(String.format(Locale.US, "%.4f", result.aucRoc), weight = 2f)
        TableCell(String.format(Locale.US, "%.4f", result.loss), weight = 2f)
        TableCell(result.executionTimeMs.toString(), weight = 2f)
    }
}

/**
 * Helper composable for creating table cells with weighting.
 */
@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    title: Boolean = false
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (title) 14.sp else 12.sp // Adjust sizes as needed
    )
}