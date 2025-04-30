package com.example.automl_prototype_1.service;

// Core model and dataprovider imports
import com.example.automl_prototype_1.algorithm.*; // Imports all algorithm classes
import com.example.automl_prototype_1.dataprovider.CsvDataProvider;
import com.example.automl_prototype_1.model.Dataset;
import com.example.automl_prototype_1.model.ExecutionResult;

// Standard Java imports
import java.io.File;
import java.io.FileReader; // <-- ADDED: Need FileReader
import java.io.IOException;
import java.io.Reader; // <-- ADDED: Need Reader
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for loading data and executing selected optimization algorithms.
 */
public class ExecutionService {

    private final CsvDataProvider csvDataProvider;
    // TODO: Add references to other data providers (e.g., ArffDataProvider) if needed

    /**
     * Constructor initializes the data providers.
     */
    public ExecutionService() {
        this.csvDataProvider = new CsvDataProvider();
        System.out.println("ExecutionService Initialized.");
    }

    /**
     * Loads data from the specified file.
     * Currently determines the provider based on file extension (only CSV supported).
     *
     * @param datasetFile The File object pointing to the dataset.
     * @return A Dataset object containing the loaded data.
     * @throws IOException If the file is invalid, unsupported, or reading fails.
     */
    public Dataset loadData(File datasetFile) throws IOException {
        if (datasetFile == null || !datasetFile.exists() || !datasetFile.isFile()) {
            throw new IOException("Invalid dataset file provided: " + (datasetFile != null ? datasetFile.getPath() : "null"));
        }

        System.out.println("ExecutionService: Attempting to load data from file: " + datasetFile.getPath());
        String fileName = datasetFile.getName().toLowerCase();

        if (fileName.endsWith(".csv")) {
            // --- FIX: Create a FileReader and pass it to CsvDataProvider ---
            System.out.println("ExecutionService: Using CsvDataProvider.");
            try (Reader fileReader = new FileReader(datasetFile)) { // Create FileReader here
                // Call the CsvDataProvider method that now expects a Reader
                return csvDataProvider.loadDataset(fileReader);
            } // FileReader is closed automatically by try-with-resources
            // -------------------------------------------------------------
        }
        // TODO: Add handlers for other file types (e.g., .arff) here
        // else if (fileName.endsWith(".arff")) { ... }
        else {
            System.err.println("ExecutionService: Unsupported file type: " + fileName);
            throw new IOException("Unsupported file type: " + fileName);
        }
    }


    /**
     * Executes the selected optimization algorithms SYNCHRONOUSLY on the provided dataset.
     * This method blocks until all selected algorithms complete or fail.
     *
     * @param dataset The loaded Dataset object.
     * @param selectedAlgorithmNames List of names of algorithms to run (must match keys in getAlgorithmImplementation).
     * @param configuration Global or algorithm-specific configurations (currently unused).
     * @return A List containing ExecutionResult objects for each completed algorithm.
     * @throws Exception If loading the dataset fails or any algorithm fails critically.
     */
    public List<ExecutionResult> executeAlgorithms(Dataset dataset,
                                                   List<String> selectedAlgorithmNames,
                                                   Map<String, Object> configuration) throws Exception { // Allow exceptions

        if (dataset == null) {
            throw new IllegalArgumentException("Dataset cannot be null for execution.");
        }
        if (selectedAlgorithmNames == null || selectedAlgorithmNames.isEmpty()) {
            System.out.println("CORE: No algorithms selected for execution.");
            return new ArrayList<>(); // Return empty list if none selected
        }

        List<ExecutionResult> allResults = new ArrayList<>();
        int totalAlgos = selectedAlgorithmNames.size();
        System.out.println("CORE: Starting synchronous execution of " + totalAlgos + " algorithms...");

        // --- Preprocessing ---
        // Currently assumed to be handled within each algorithm's execute method.
        // System.out.println("CORE: Preprocessing step (if any) assumed within algorithms.");

        // --- Algorithm Execution Loop ---
        for (int i = 0; i < totalAlgos; i++) {
            String algoName = selectedAlgorithmNames.get(i);
            System.out.println("CORE: === Running " + algoName + " [" + (i + 1) + "/" + totalAlgos + "] ===");

            OptimizationAlgorithm algorithm = getAlgorithmImplementation(algoName);

            if (algorithm != null) {
                try {
                    long startTime = System.currentTimeMillis();
                    // Execute the algorithm directly, passing the Dataset object
                    ExecutionResult result = algorithm.execute(dataset, configuration);
                    long endTime = System.currentTimeMillis();
                    System.out.println("CORE: Algorithm " + algoName + " completed in " + (endTime - startTime) + " ms.");
                    allResults.add(result);

                } catch (Exception e) {
                    // Log the error and re-throw to signal failure to the caller (ViewModel)
                    System.err.println("CORE: CRITICAL ERROR executing algorithm: " + algoName);
                    e.printStackTrace(); // Log stack trace for details
                    throw e; // Propagate the exception
                }
            } else {
                System.err.println("CORE: Implementation not found for algorithm: " + algoName + ". Skipping.");
                // Add a placeholder result to indicate it was skipped
                allResults.add(new ExecutionResult(algoName + " (Skipped - Not Found)", Double.NaN, Double.NaN, Double.NaN, 0));
            }
        } // End loop

        System.out.println("CORE: All algorithm executions finished. Results count: " + allResults.size());
        return allResults; // Return the collected results
    }


    /**
     * Factory method to get an instance of an algorithm based on its name.
     * (Implementation remains the same)
     *
     * @param name The name of the algorithm.
     * @return An instance of OptimizationAlgorithm, or null if not found.
     */
    private OptimizationAlgorithm getAlgorithmImplementation(String name) {
        if (name == null) return null;
        // Using equalsIgnoreCase for robustness
        if ("Genetic Algorithm".equalsIgnoreCase(name)) return new GeneticAlgorithm();
        if ("IGPSO".equalsIgnoreCase(name)) return new MockIgpsoAlgorithm();
        if ("WWO".equalsIgnoreCase(name)) return new MockWwoAlgorithm();
        if ("BPSO".equalsIgnoreCase(name)) return new MockBpsoAlgorithm();
        if ("ASO".equalsIgnoreCase(name)) return new MockAsoAlgorithm();
        if ("NNP".equalsIgnoreCase(name)) return new MockNnpAlgorithm();
        // Add other real or mock algorithms here
        System.err.println("ExecutionService: No implementation found for algorithm name '" + name + "'");
        return null; // Explicitly return null if no match
    }
}