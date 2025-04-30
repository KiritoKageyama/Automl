package com.shadowtengu.automl.androidapp.ui.vm // Ensure this package is correct

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Core library imports
import com.example.automl_prototype_1.dataprovider.CsvDataProvider
import com.example.automl_prototype_1.model.Dataset
import com.example.automl_prototype_1.service.AppStateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File // Still needed for AppStateService and temp file creation/deletion
import java.io.FileOutputStream // Required for copy helper
import java.io.InputStreamReader // *** Required for creating a Reader ***
import java.io.IOException
import java.lang.Exception

// --- UploadScreenState Definition (No changes needed here) ---
sealed class UploadScreenState {
    object Idle : UploadScreenState()
    data class FileSelected(val fileName: String, val uri: Uri) : UploadScreenState()
    object Processing : UploadScreenState() // Reading file / Calling core library
    data class DataLoadSuccess(val fileName: String, val storedFilePath: String) : UploadScreenState() // Success means File path stored
    data class Error(val message: String) : UploadScreenState()
}
// --- End UploadScreenState Definition ---


class UploadViewModel : ViewModel() {

    var uiState by mutableStateOf<UploadScreenState>(UploadScreenState.Idle)
        private set

    // --- onFileSelected (No changes needed here) ---
    fun onFileSelected(uri: Uri?, fileName: String?) {
        if (uri != null && fileName != null) {
            Log.d("UploadViewModel", "[onFileSelected] File selected: $fileName, URI: $uri")
            uiState = UploadScreenState.FileSelected(fileName, uri)
            AppStateService.getInstance().selectedDatasetFile = null
            Log.d("UploadViewModel", "[onFileSelected] Cleared dataset file path in AppStateService.")
        } else {
            Log.w("UploadViewModel", "[onFileSelected] File selection cancelled or failed.")
            if (uiState !is UploadScreenState.DataLoadSuccess && uiState !is UploadScreenState.Processing) {
                uiState = UploadScreenState.Idle
            }
            AppStateService.getInstance().selectedDatasetFile = null
        }
    }

    /**
     * Called when the user clicks the "Prepare & Load Data" button.
     * Reads the file content using a Reader, calls the core CsvDataProvider (expecting Reader),
     * and if successful, copies the original file content to a temporary file
     * for AppStateService compatibility. Updates the UI state throughout.
     * @param context The application context.
     */
    fun prepareAndLoadData(context: Context) {
        val currentState = uiState
        if (currentState is UploadScreenState.FileSelected) {
            Log.i("UploadViewModel", "[prepareAndLoadData] Processing selection for file: ${currentState.fileName}")
            uiState = UploadScreenState.Processing

            viewModelScope.launch {

                var tempFileForStorage: File? = null
                var datasetResult: Dataset? = null

                // Use Dispatchers.IO for stream operations and potentially temp file writing
                datasetResult = withContext(Dispatchers.IO) {
                    var reader: InputStreamReader? = null
                    var currentTempFileForStorage: File? = null // Local var for temp file scope
                    try {
                        // Step 1: Open InputStream from URI and create a Reader
                        Log.d("UploadViewModel", "[prepareAndLoadData] Opening InputStream for URI: ${currentState.uri}")
                        val parsedDataset = context.contentResolver.openInputStream(currentState.uri)?.let { inputStream ->
                            reader = InputStreamReader(inputStream) // Create the reader
                            Log.d("UploadViewModel", "[prepareAndLoadData] InputStreamReader created from URI.")

                            // Step 2: Call core library's CsvDataProvider with the Reader
                            Log.i("UploadViewModel", "[prepareAndLoadData] Parsing CSV data using CsvDataProvider with Reader...")
                            val dataProvider = CsvDataProvider()
                            // *** THIS IS THE CALL EXPECTING A READER ***
                            dataProvider.loadDataset(reader) // PASSING THE READER
                            // ***------------------------------------***
                        } ?: throw IOException("Failed to open input stream for URI: ${currentState.uri}")

                        // Step 3: If parsing succeeded, create the temporary File for AppStateService
                        if (parsedDataset != null) {
                            Log.i("UploadViewModel", "[prepareAndLoadData] Core library parsed data successfully. Creating temp file...")
                            currentTempFileForStorage = copyUriToTempFile(context, currentState.uri, currentState.fileName)
                            if (currentTempFileForStorage == null) {
                                throw IOException("Failed to create temporary file for AppStateService after successful parsing.")
                            }
                            Log.i("UploadViewModel", "[prepareAndLoadData] Created temp file for AppStateService: ${currentTempFileForStorage.absolutePath}")
                            tempFileForStorage = currentTempFileForStorage // Assign to outer var ONLY on success
                        } else {
                            throw IOException("CsvDataProvider returned null Dataset without throwing Exception.")
                        }
                        parsedDataset // Return the parsed Dataset object

                    } catch (e: Exception) {
                        Log.e("UploadViewModel", "[prepareAndLoadData] Error during processing", e)
                        currentTempFileForStorage?.delete() // Clean up temp file if created before error
                        null // Return null on failure
                    } finally {
                        // Close the reader regardless of success or failure
                        try {
                            reader?.close()
                            Log.d("UploadViewModel", "[prepareAndLoadData] InputStreamReader closed.")
                        } catch (closeException: IOException) { Log.w("UploadViewModel", "[prepareAndLoadData] Error closing reader", closeException) }
                    }
                } // End withContext(Dispatchers.IO)

                // --- Update Final UI State ---
                val finalTempFile = tempFileForStorage
                if (datasetResult != null && finalTempFile != null) {
                    Log.i("UploadViewModel", "[prepareAndLoadData] Success. Storing temp file path in AppStateService.")
                    AppStateService.getInstance().selectedDatasetFile = finalTempFile // Store the File object
                    uiState = UploadScreenState.DataLoadSuccess(currentState.fileName, finalTempFile.absolutePath)
                } else {
                    Log.e("UploadViewModel", "[prepareAndLoadData] Processing failed (Dataset or Temp File is null).")
                    uiState = UploadScreenState.Error("Failed to process data file.")
                    AppStateService.getInstance().selectedDatasetFile = null
                    // Ensure temp file is deleted if it exists but dataset is null (shouldn't happen often)
                    finalTempFile?.delete()
                }
            } // End viewModelScope.launch
        } else {
            Log.w("UploadViewModel", "[prepareAndLoadData] Request ignored, state is not FileSelected: $currentState")
        }
    }

    // --- Helper Function copyUriToTempFile (No changes needed) ---
    private suspend fun copyUriToTempFile(context: Context, uri: Uri, originalFileName: String): File? =
        withContext(Dispatchers.IO) {
            // ... (Implementation remains the same as previous correct version) ...
            var tempFile: File? = null
            try {
                val cacheDir = context.cacheDir
                val extension = originalFileName.substringAfterLast('.', "")
                val prefix = originalFileName.substringBeforeLast('.', originalFileName)
                    .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    .take(50)
                tempFile = File.createTempFile("${prefix}_", ".$extension", cacheDir)
                Log.d("UploadViewModel", "[copyUriToTempFile] Creating temp file: ${tempFile.absolutePath}")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                        Log.d("UploadViewModel", "[copyUriToTempFile] Finished copying data.")
                    }
                } ?: throw IOException("Failed to open input stream for URI: $uri")
                tempFile
            } catch (e: Exception) {
                Log.e("UploadViewModel", "[copyUriToTempFile] Error copying URI to temp file", e)
                tempFile?.delete()
                null
            }
        }

    // --- clearSelection Function (No changes needed) ---
    fun clearSelection() {
        // ... (Implementation remains the same as previous correct version) ...
        val fileToClear = AppStateService.getInstance().selectedDatasetFile
        uiState = UploadScreenState.Idle
        AppStateService.getInstance().selectedDatasetFile = null
        Log.d("UploadViewModel", "[clearSelection] Cleared dataset file path in AppStateService.")
        if (fileToClear != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    if (fileToClear.exists()) {
                        if (fileToClear.delete()) {
                            Log.i("UploadViewModel", "[clearSelection] Deleted temp file: ${fileToClear.absolutePath}")
                        } else {
                            Log.w("UploadViewModel", "[clearSelection] Failed to delete temp file: ${fileToClear.absolutePath}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UploadViewModel", "[clearSelection] Error deleting temp file", e)
                }
            }
        }
    }
}

// Need FileOutputStream for the copy helper function
