package com.shadowtengu.automl.androidapp.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// Import navigation destinations for LaunchedEffect
import com.shadowtengu.automl.androidapp.ui.navigation.AppDestinations
// Import state and ViewModel
import com.shadowtengu.automl.androidapp.ui.vm.UploadScreenState
import com.shadowtengu.automl.androidapp.ui.vm.UploadViewModel
import java.io.File // Example import - not directly used in UI logic

/**
 * Composable function for the Upload Screen UI.
 * Allows users to select a file and initiate the data preparation process.
 * Observes state from UploadViewModel to display relevant UI elements.
 * Handles navigation automatically upon successful processing.
 *
 * @param navController The NavController for handling navigation events.
 * @param uploadViewModel The ViewModel instance associated with this screen.
 */
@Composable
fun UploadScreen(
    navController: NavController,
    uploadViewModel: UploadViewModel = viewModel() // Obtain ViewModel instance
) {
    // Log to confirm this specific composable is being executed
    Log.d("WHICH_SCREEN", "--- Rendering DETAILED UploadScreen (Reader Version) ---") // Updated log slightly

    // Observe the UI state from the ViewModel. Use rememberUpdatedState to ensure
    // LaunchedEffect captures the latest state if recomposition happens quickly.
    val uiState by rememberUpdatedState(uploadViewModel.uiState)
    val context = LocalContext.current // Get context for file name retrieval

    // Set up the ActivityResultLauncher for the file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Use standard contract
    ) { uri: Uri? -> // This lambda executes when the picker returns
        val fileName = uri?.let { getFileName(context, it) } // Get name safely
        uploadViewModel.onFileSelected(uri, fileName) // Notify ViewModel
    }

    // --- LaunchedEffect for Automatic Navigation ---
    // This side effect runs whenever the `uiState` changes.
    // If the state becomes DataLoadSuccess, it triggers navigation.
    LaunchedEffect(key1 = uiState) { // Re-evaluate when uiState changes
        if (uiState is UploadScreenState.DataLoadSuccess) {
            Log.i("UploadScreen", "Data load successful state detected, navigating to algorithm selection.")
            // Navigate to the next screen in the defined navigation graph
            navController.navigate(AppDestinations.ALGORITHM_SELECTION_ROUTE) {
                // Remove the UploadScreen from the back stack after navigating away
                popUpTo(AppDestinations.UPLOAD_ROUTE) { inclusive = true }
            }
            // Optional: Notify ViewModel that navigation has occurred if state reset is needed
            // uploadViewModel.navigationHandled()
        }
    }
    // --- End LaunchedEffect ---

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize() // Use all available screen space
            .padding(16.dp), // Apply padding around the content
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        // --- Screen Title ---
        Text(
            text = "Upload Your Data",
            style = MaterialTheme.typography.headlineMedium // Use predefined text style
        )

        Spacer(modifier = Modifier.height(24.dp)) // Vertical spacing

        // --- File Selection Area ---
        // Display either a loading indicator or the 'Select File' button
        when (uiState) {
            is UploadScreenState.Processing -> {
                CircularProgressIndicator() // Show loading animation
                Spacer(modifier = Modifier.height(8.dp)) // Space below spinner
            }
            else -> {
                // Show the button in all other states (Idle, FileSelected, DataLoadSuccess, Error)
                Button(
                    onClick = {
                        // Launch the file picker when clicked
                        filePickerLauncher.launch("*/*") // Allow selection of any file type
                    },
                    // Button is enabled only when the state is Idle (no file selected yet)
                    enabled = uiState is UploadScreenState.Idle
                ) {
                    Text("Select File")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Vertical spacing

        // --- Status Display Area ---
        // Show informative text based on the current state
        when (val state = uiState) { // Assign state to local val for easier access
            is UploadScreenState.Idle -> {
                Text("No file selected", style = MaterialTheme.typography.bodyLarge)
            }
            is UploadScreenState.FileSelected -> {
                // Display the name of the file the user has picked
                Text("Selected: ${state.fileName}", style = MaterialTheme.typography.bodyLarge)
            }
            is UploadScreenState.Processing -> {
                // Inform the user that work is happening
                Text("Preparing & Loading Data...", style = MaterialTheme.typography.bodyLarge) // Updated text
            }
            is UploadScreenState.DataLoadSuccess -> {
                // Show confirmation (briefly visible before navigation)
                Text("Data ready for: ${state.fileName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            is UploadScreenState.Error -> {
                // Display the error message from the ViewModel
                Text("Error: ${state.message}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                // Allow user to clear the error and try again
                Button(onClick = { uploadViewModel.clearSelection() }) {
                    Text("Clear / Retry")
                }
            }
        } // End when for status text

        // --- Action Button Area ---
        // Show the "Prepare & Load Data" button only when a file has been selected
        if (uiState is UploadScreenState.FileSelected) {
            Spacer(modifier = Modifier.height(32.dp)) // Add space before the button
            Button(
                onClick = {
                    // *** FIX: Call the correct ViewModel function name ***
                    uploadViewModel.prepareAndLoadData(context)
                    // ***-------------------------------------------***
                }
                // Button is implicitly enabled because it only appears in this state
            ) {
                Text("Prepare & Load Data") // Text reflects the action
            }
        }
        // Navigation is triggered by LaunchedEffect based on state change, not this button.

    } // End Column
}

/**
 * Helper function to extract a displayable file name from a content URI.
 * Includes basic error handling and fallbacks.
 * @param context The application context.
 * @param uri The content URI of the file.
 * @return The display name string, or a fallback name if retrieval fails.
 */
private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else {
                    uri.lastPathSegment?.substringAfterLast('/') ?: "unknown_file"
                }
            } else {
                "unknown_file_empty_cursor"
            }
        } ?: "unknown_file_null_cursor"
    } catch (e: Exception) {
        Log.e("UploadScreen", "Error getting file name from URI: $uri", e)
        uri.lastPathSegment?.substringAfterLast('/') ?: "unknown_file_on_error"
    }
}