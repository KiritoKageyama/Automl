package com.example.automl_prototype_1.algorithm;

import com.example.automl_prototype_1.model.Dataset;
import com.example.automl_prototype_1.model.ExecutionResult;
import java.util.Map;
import java.io.File; // Keep import for usage

/**
 * Abstract base class for mock algorithms to provide common execution simulation.
 */
public abstract class AbstractMockAlgorithm implements OptimizationAlgorithm {

    // --- Abstract methods to be implemented by concrete subclasses ---

    /**
     * Concrete subclasses must provide their specific algorithm name.
     * FIX: Changed from protected to public to match interface.
     * @return The name of the algorithm.
     */
    public abstract String getAlgorithmName();

    /**
     * Concrete subclasses must provide their simulated execution time.
     * @return The simulated execution time in milliseconds.
     */
    protected abstract long getMockExecutionTime(); // protected is fine here


    /**
     * Default constructor (implicitly exists if no other constructors are defined).
     * Subclasses will call this implicitly if they don't call another super constructor.
     */
    public AbstractMockAlgorithm() {
        // No specific initialization needed in the abstract class constructor itself
    }


    /**
     * Simulates executing an algorithm on the dataset.
     * Handles potential null sourceFile within the Dataset safely.
     * Generates mock results based on random values and simulated time.
     */
    @Override
    public ExecutionResult execute(Dataset dataset, Map<String, Object> configuration) {
        // --- Safely get dataset source name ---
        String datasetSourceName;
        File sourceFile = dataset.getSourceFile(); // Assuming getSourceFile() exists

        if (sourceFile != null) {
            datasetSourceName = sourceFile.getName();
        } else {
            datasetSourceName = "Stream/Reader Input"; // Default for non-file sources
        }
        // -------------------------------------

        System.out.println("Executing mock algorithm: " + getAlgorithmName() + " on dataset source: " + datasetSourceName);
        // Assuming these methods exist on Dataset:
        System.out.println("Dataset has " + dataset.getRecordCount() + " records and " + dataset.getFeatureCount() + " features.");

        // --- Simulate execution time ---
        long executionTime = getMockExecutionTime(); // Get time from subclass
        try {
            if (executionTime > 0) {
                System.out.println(getAlgorithmName() + " mock execution sleeping for " + executionTime + " ms...");
                Thread.sleep(executionTime); // Simulate work
            } else {
                System.out.println(getAlgorithmName() + " mock execution has zero simulation time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Set the interrupt flag
            System.err.println(getAlgorithmName() + " execution interrupted.");
            // Return a specific result indicating interruption
            return new ExecutionResult(getAlgorithmName() + " (Interrupted)", Double.NaN, Double.NaN, Double.NaN, 0);
        }

        // --- Simulate generating mock results ---
        // Use algorithm name hashcode for slightly more deterministic randomness per algo
        int nameHash = getAlgorithmName().hashCode();
        java.util.Random random = new java.util.Random(nameHash); // Seed random based on name
        double mockAccuracy = 0.80 + (random.nextDouble() * 0.18); // 80-98%
        double mockAuc = 0.85 + (random.nextDouble() * 0.14);  // 85-99%
        double mockLoss = 0.02 + (random.nextDouble() * 0.1);   // Small loss value

        // Ensure values are within reasonable bounds (optional)
        mockAccuracy = Math.min(1.0, Math.max(0.0, mockAccuracy));
        mockAuc = Math.min(1.0, Math.max(0.0, mockAuc));
        mockLoss = Math.max(0.0, mockLoss);


        System.out.println(getAlgorithmName() + " mock execution complete.");
        // Return the simulated results
        return new ExecutionResult(
                getAlgorithmName(),
                mockAccuracy,
                mockAuc,
                mockLoss,
                executionTime // Return the actual simulated duration
        );
    }
}