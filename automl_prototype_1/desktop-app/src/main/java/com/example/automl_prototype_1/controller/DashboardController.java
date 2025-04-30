package com.example.automl_prototype_1.controller;

import com.example.automl_prototype_1.model.Dataset;
import com.example.automl_prototype_1.model.ExecutionResult;
import com.example.automl_prototype_1.service.AppStateService;
import com.example.automl_prototype_1.service.ExecutionService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map; // Keep if needed for future config

public class DashboardController {

    @FXML private Button uploadBtn;
    @FXML private Button algoBtn;
    @FXML private Button runBtn;
    @FXML private Button resultsBtn;
    @FXML private Button settingsBtn;
    @FXML private StackPane mainContent;
    @FXML private Button exitBtn;

    private ExecutionService executionService;
    private AppStateService appStateService;

    // No persistent progress UI elements needed here

    @FXML
    private void initialize() {
        System.out.println("Dashboard Initialized");
        this.executionService = new ExecutionService(); // Create instance
        this.appStateService = AppStateService.getInstance(); // Get singleton instance

        loadView("upload_view.fxml"); // Load initial view

        // Setup button actions
        uploadBtn.setOnAction(event -> loadView("upload_view.fxml"));
        algoBtn.setOnAction(event -> loadView("algo_select_view.fxml"));
        runBtn.setOnAction(event -> handleRunModel());
        resultsBtn.setOnAction(event -> loadView("results_view.fxml"));
        settingsBtn.setOnAction(event -> loadView("settings_view.fxml"));
        exitBtn.setOnAction(event -> onExitClicked()); // Make sure exit button is linked
    }

    /**
     * Handles the action event for the "Run Model" button.
     * Validates state, shows progress UI, runs algorithms in a background task,
     * stores results in AppStateService on success, and navigates to the results view.
     */
    private void handleRunModel() {
        System.out.println("Run Model button clicked.");

        // Retrieve necessary state from the shared service
        File datasetFile = appStateService.getSelectedDatasetFile();
        List<String> algorithms = appStateService.getSelectedAlgorithmNames();

        // Validate state before proceeding
        if (datasetFile == null) {
            showError("Please select a dataset file first.");
            loadView("upload_view.fxml"); // Guide user back
            return;
        }
        if (algorithms == null || algorithms.isEmpty()) {
            showError("Please select at least one algorithm.");
            loadView("algo_select_view.fxml"); // Guide user back
            return;
        }

        // --- Show Progress UI Dynamically ---
        // Create new controls each time to avoid issues with reusing nodes
        ProgressIndicator currentProgressIndicator = new ProgressIndicator(-1.0); // Indeterminate progress
        Label currentProgressLabel = new Label("Starting execution..."); // Initial message
        currentProgressLabel.setStyle("-fx-font-size: 14px;");
        VBox currentProgressBox = new VBox(20, currentProgressLabel, currentProgressIndicator);
        currentProgressBox.setAlignment(Pos.CENTER);
        currentProgressBox.setPadding(new Insets(50));
        mainContent.getChildren().setAll(currentProgressBox); // Replace main content with progress view
        System.out.println("Showing progress view.");
        runBtn.setDisable(true); // Disable run button during execution
        // ----------------------------------

        // --- Create Background Task for Execution ---
        Task<List<ExecutionResult>> executionTask = new Task<>() {
            @Override
            protected List<ExecutionResult> call() throws Exception {
                // This code runs on a background thread

                // 1. Update progress message and load the Dataset object
                updateMessage("Loading data: " + datasetFile.getName());
                // ExecutionService handles loading from File internally
                Dataset dataset = executionService.loadData(datasetFile);

                // 2. Update progress message and execute algorithms
                updateMessage("Running selected algorithms...");
                // Pass loaded dataset, algorithm names, and empty config map for now
                // ExecutionService handles the core logic
                List<ExecutionResult> results = executionService.executeAlgorithms(dataset, algorithms, null); // Using null for config

                // 3. Update progress message (briefly shown before success handler runs)
                updateMessage("Execution complete.");
                return results; // Return the results list
            }
        };

        // --- Task Event Handlers (Run on JavaFX Application Thread via Platform.runLater implicitly) ---

        // Bind the progress indicator and label to the task's progress and message properties
        updateProgressViewBindings(executionTask, currentProgressIndicator, currentProgressLabel);

        // --- On Success ---
        executionTask.setOnSucceeded(event -> {
            // This code runs on the JavaFX Application Thread after task finishes successfully
            Platform.runLater(() -> { // Explicit Platform.runLater is good practice, though often implicit here
                List<ExecutionResult> results = executionTask.getValue(); // Get results from task
                System.out.println("Background task succeeded. Results count: " + (results != null ? results.size() : "null"));

                // ---!!! STORE RESULTS IN SHARED SERVICE !!!---
                AppStateService.getInstance().setExecutionResults(results);
                // ---------------------------------------------

                runBtn.setDisable(false); // Re-enable run button
                // Load the results view. ResultsViewController will fetch results from AppStateService.
                loadView("results_view.fxml");
            });
        });

        // --- On Failure ---
        executionTask.setOnFailed(event -> {
            // This code runs on the JavaFX Application Thread if task throws an exception
            Platform.runLater(() -> {
                Throwable exception = executionTask.getException();
                System.err.println("Background task failed:");
                if (exception != null) exception.printStackTrace();

                // --- Clear results in shared service on failure ---
                AppStateService.getInstance().setExecutionResults(null);
                // -------------------------------------------------

                // Show error message in the UI, replacing the progress view
                showError("Execution failed: " + (exception != null ? exception.getMessage() : "Unknown error"));
                runBtn.setDisable(false); // Re-enable run button
                // Optionally navigate back: loadView("algo_select_view.fxml");
            });
        });

        // --- On Cancellation ---
        executionTask.setOnCancelled(event -> {
            // This code runs on the JavaFX Application Thread if task is cancelled
            Platform.runLater(() -> {
                System.out.println("Background task cancelled.");
                // Clear results in shared service on cancellation
                AppStateService.getInstance().setExecutionResults(null);
                // Show cancellation message
                showError("Execution cancelled by user.");
                runBtn.setDisable(false); // Re-enable run button
                // Optionally navigate back: loadView("algo_select_view.fxml");
            });
        });

        // --- Start the Background Task ---
        Thread backgroundThread = new Thread(executionTask);
        backgroundThread.setDaemon(true); // Allow JVM to exit even if task is running
        backgroundThread.start();
        System.out.println("Execution task started in background.");
    }

    // --- UI Update Methods ---

    /**
     * Binds the progress indicator and label to the properties of a background Task.
     * @param task The background task providing progress and message updates.
     * @param indicator The ProgressIndicator UI element.
     * @param label The Label UI element to display task messages.
     */
    private void updateProgressViewBindings(Task<?> task, ProgressIndicator indicator, Label label) {
        if (indicator != null && label != null && task != null) {
            // Unbind previous bindings just in case (though usually not necessary with new controls)
            indicator.progressProperty().unbind();
            label.textProperty().unbind();

            // Bind to the task's properties
            indicator.progressProperty().bind(task.progressProperty()); // For determinate progress (-1 means indeterminate)
            label.textProperty().bind(task.messageProperty()); // Updates label text as task calls updateMessage()
            System.out.println("Bound progress UI to task.");
        } else {
            System.err.println("Could not bind progress view elements (null passed?).");
        }
    }

    /**
     * Loads a specified FXML view into the main content area.
     * Handles potential loading errors.
     * @param fxmlFile The name of the FXML file (e.g., "upload_view.fxml").
     */
    private void loadView(String fxmlFile) {
        runBtn.setDisable(false); // Ensure run button is generally enabled when switching views
        try {
            String fullPath = "/com/example/automl_prototype_1/" + fxmlFile;
            URL fxmlUrl = getClass().getResource(fullPath);
            if (fxmlUrl == null) {
                showError("Cannot find FXML resource: " + fullPath);
                return;
            }
            System.out.println("Attempting to load view: " + fullPath);
            // Load the FXML and set it as the content of the StackPane
            Parent viewRoot = FXMLLoader.load(fxmlUrl);
            mainContent.getChildren().setAll(viewRoot);
            System.out.println("Successfully loaded view: " + fxmlFile);
        } catch (IOException e) {
            showError("Error loading view " + fxmlFile);
            e.printStackTrace();
        } catch (Exception e) {
            showError("Unexpected error loading view " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Displays an error message in the main content area, replacing existing content.
     * @param message The error message to display.
     */
    private void showError(String message) {
        System.err.println("UI Error: " + message);
        Label errorLabel = new Label("Error: " + message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-padding: 20px;");
        errorLabel.setWrapText(true); // Allow text wrapping
        VBox errorBox = new VBox(errorLabel); // Use a layout pane for alignment/padding
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(20));
        // Replace the main content area with this error message
        mainContent.getChildren().setAll(errorBox);
    }

    /**
     * Handles the action for the Exit button. Terminates the application.
     */
    @FXML private void onExitClicked() {
        System.out.println("Exit button clicked. Terminating application.");
        Platform.exit(); // Gracefully exit JavaFX application
        System.exit(0); // Ensure JVM terminates
    }
}