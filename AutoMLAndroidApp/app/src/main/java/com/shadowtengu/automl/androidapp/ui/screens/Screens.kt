// In MainActivity.kt or a new ui/screens/Screens.kt file
package com.shadowtengu.automl.androidapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
//import com.shadowtengu.automl.androidapp.ui.screens.UploadScreen


/*@Composable
fun UploadScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upload Screen")
        // TODO: Add Upload Button and File Handling Logic
    }
}
*/




@Composable
fun SettingsScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings Screen")
        // TODO: Add any app settings
    }
    /*@Composable
    fun UploadScreenPlaceholder(navController: NavHostController) { // Rename if needed
        // Call the real UploadScreen from its own file
        UploadScreen(navController = navController)
    }*/
}