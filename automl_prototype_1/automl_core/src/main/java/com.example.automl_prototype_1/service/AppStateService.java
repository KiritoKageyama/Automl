package com.example.automl_prototype_1.service;

import com.example.automl_prototype_1.model.Dataset; // If you decide to store Dataset later
import com.example.automl_prototype_1.model.ExecutionResult; // Required for results list
import java.io.File; // Required as we are storing the File reference
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service to hold shared application state like the selected dataset file reference,
 * selected algorithm names, base model name, and execution results.
 * This is a simple approach suitable for prototypes.
 * For larger applications, consider dependency injection frameworks.
 */
public class AppStateService {

    // --- Singleton Instance Holder ---
    private static AppStateService instance;

    // --- Shared State Fields ---

    // Holds the reference to the temporary file created by the Android app
    private File selectedDatasetFile;

    // Holds the names of the algorithms selected by the user
    private List<String> selectedAlgorithmNames;

    // Holds the name of the base model (if applicable, otherwise can be null)
    private String selectedBaseModelName;

    // Holds the list of results after algorithms are executed
    private List<ExecutionResult> executionResults;


    // --- Private Constructor ---
    // Prevents external instantiation, ensuring only one instance exists.
    private AppStateService() {
        selectedAlgorithmNames = new ArrayList<>(); // Initialize the list
        executionResults = null; // Initialize results as null (or empty list if preferred)
        System.out.println("AppStateService Initialized (Singleton)");
    }

    // --- Get Singleton Instance ---
    /**
     * Provides access to the single instance of AppStateService.
     * Creates the instance if it doesn't exist yet (thread-safe).
     * @return The singleton AppStateService instance.
     */
    public static synchronized AppStateService getInstance() {
        if (instance == null) {
            instance = new AppStateService();
        }
        return instance;
    }

    // --- Getters and Setters for Shared State ---

    /**
     * Gets the reference to the selected dataset file.
     * @return The File object representing the dataset, or null if none is set.
     */
    public File getSelectedDatasetFile() {
        return selectedDatasetFile;
    }

    /**
     * Sets the reference to the selected dataset file.
     * @param selectedDatasetFile The File object representing the dataset.
     */
    public void setSelectedDatasetFile(File selectedDatasetFile) {
        this.selectedDatasetFile = selectedDatasetFile;
        if (selectedDatasetFile != null) {
            System.out.println("AppStateService: Dataset file set to -> " + selectedDatasetFile.getPath());
        } else {
            System.out.println("AppStateService: Dataset file cleared.");
        }
    }

    /**
     * Gets the list of selected algorithm names.
     * @return A new list containing the names of the selected algorithms.
     */
    public List<String> getSelectedAlgorithmNames() {
        // Return a copy to prevent external modification of the internal list
        return new ArrayList<>(selectedAlgorithmNames);
    }

    /**
     * Sets the list of selected algorithm names.
     * @param selectedAlgorithmNames A list containing the names of the algorithms to be executed.
     */
    public void setSelectedAlgorithmNames(List<String> selectedAlgorithmNames) {
        if (selectedAlgorithmNames != null) {
            this.selectedAlgorithmNames = new ArrayList<>(selectedAlgorithmNames); // Store a copy
            System.out.println("AppStateService: Algorithms set to -> " + this.selectedAlgorithmNames);
        } else {
            this.selectedAlgorithmNames = new ArrayList<>();
            System.out.println("AppStateService: Algorithms cleared.");
        }
    }

    /**
     * Gets the name of the selected base model.
     * @return The name of the base model, or null if not set.
     */
    public String getSelectedBaseModelName() {
        return selectedBaseModelName;
    }

    /**
     * Sets the name of the selected base model.
     * @param selectedBaseModelName The name of the base model.
     */
    public void setSelectedBaseModelName(String selectedBaseModelName) {
        this.selectedBaseModelName = selectedBaseModelName;
        if (selectedBaseModelName != null) {
            System.out.println("AppStateService: Base Model set to -> " + this.selectedBaseModelName);
        } else {
            System.out.println("AppStateService: Base Model cleared.");
        }
    }

    /**
     * Gets the list of execution results from the last algorithm run.
     * @return A new list containing the ExecutionResult objects, or null if no results are available.
     */
    public List<ExecutionResult> getExecutionResults() {
        // Return a copy to prevent external modification and ensure null safety
        return executionResults == null ? null : new ArrayList<>(executionResults);
    }

    /**
     * Sets the list of execution results after algorithms have run.
     * @param executionResults The list of ExecutionResult objects, or null to clear.
     */
    public void setExecutionResults(List<ExecutionResult> executionResults) {
        this.executionResults = executionResults; // Allow setting null directly
        if (executionResults != null) {
            System.out.println("AppStateService: Execution Results set. Count: " + executionResults.size());
        } else {
            System.out.println("AppStateService: Execution Results cleared.");
        }
    }

    // --- State Management ---

    /**
     * Clears all stored state (dataset file, algorithms, base model, results).
     */
    public void clearState() {
        this.selectedDatasetFile = null;
        this.selectedAlgorithmNames.clear();
        this.selectedBaseModelName = null;
        this.executionResults = null; // Ensure results are cleared
        System.out.println("AppStateService: State cleared.");
    }
}