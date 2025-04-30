package com.shadowtengu.automl.androidapp.ui // Package is okay here

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api // Keep OptIn if using experimental Scaffold features
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController // Explicit import can sometimes help clarity

// --- Ensure these imports correctly point to your files ---
import com.shadowtengu.automl.androidapp.ui.navigation.AppDestinations
import com.shadowtengu.automl.androidapp.ui.screens.AlgorithmSelectionScreen
import com.shadowtengu.automl.androidapp.ui.screens.ResultsScreen
import com.shadowtengu.automl.androidapp.ui.screens.SettingsScreen
import com.shadowtengu.automl.androidapp.ui.screens.UploadScreen
import com.shadowtengu.automl.androidapp.ui.theme.AutoMLAndroidAppTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoMLAndroidAppTheme {
                // AppScreen manages the main layout and navigation
                AppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Keep if using experimental Scaffold features
@Composable
fun AppScreen() {
    val navController: NavHostController = rememberNavController()

    Scaffold(
        // Optional: Add TopAppBar, BottomNavigationBar etc. here later
        // topBar = { /* Your TopAppBar */ },
        // bottomBar = { /* Your BottomNavBar */ }
    ) { innerPadding -> // innerPadding is provided by Scaffold for content placement
        NavHost(
            navController = navController,
            startDestination = AppDestinations.UPLOAD_ROUTE, // Ensure this constant exists in AppDestinations
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            // --- Define navigation routes ---

            // Upload Screen Route
            composable(route = AppDestinations.UPLOAD_ROUTE) { // Ensure UPLOAD_ROUTE exists
                UploadScreen(navController = navController)
            }

            // Algorithm Selection Screen Route
            composable(route = AppDestinations.ALGORITHM_SELECTION_ROUTE) { // Ensure ALGORITHM_SELECTION_ROUTE exists
                // Make sure AlgorithmSelectionScreen Composable exists and is imported
                AlgorithmSelectionScreen(navController = navController)
            }

            // Results Screen Route
            composable(route = AppDestinations.RESULTS_ROUTE) { // Ensure RESULTS_ROUTE exists
                // Make sure ResultsScreen Composable exists and is imported
                ResultsScreen(navController = navController)
            }

            // Settings Screen Route
            composable(route = AppDestinations.SETTINGS_ROUTE) { // Ensure SETTINGS_ROUTE exists
                // Make sure SettingsScreen Composable exists and is imported
                SettingsScreen(navController = navController)
            }

            // --- Remove the duplicate UploadScreen composable block ---
            // composable(AppDestinations.UPLOAD_ROUTE) { // <<< DELETE THIS BLOCK
            //     UploadScreen(navController = navController)
            // }

            // Add more composable(...) blocks for any other screens
        }
    }
}

// No need for Greeting or GreetingPreview if you removed them