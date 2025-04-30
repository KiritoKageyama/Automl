package com.example.automl_prototype_1.model;

// *** NO JavaFX imports here ***

/**
 * Represents results - Plain Java Object (POJO) for the core module.
 * UI modules can wrap this in properties if needed for binding.
 */
public class ExecutionResult {

    // --- Use standard Java types ---
    private final String algorithmName;
    private final double accuracy;
    private final double aucRoc;
    private final double loss;
    private final long executionTimeMs;
    // Add other standard fields for metrics as needed


    // Constructor remains the same, just assigns to plain fields
    public ExecutionResult(String algorithmName, double accuracy, double aucRoc, double loss, long executionTimeMs) {
        this.algorithmName = algorithmName;
        this.accuracy = accuracy;
        this.aucRoc = aucRoc;
        this.loss = loss;
        this.executionTimeMs = executionTimeMs;
    }

    // --- Standard Getters ---
    public String getAlgorithmName() { return algorithmName; }
    public double getAccuracy() { return accuracy; }
    public double getAucRoc() { return aucRoc; }
    public double getLoss() { return loss; }
    public long getExecutionTimeMs() { return executionTimeMs; }

    // Add getters for other metrics if added

    @Override
    public String toString() {
        // Simple toString for logging/debugging
        return "ExecutionResult{" +
                "algo='" + algorithmName + '\'' +
                ", acc=" + accuracy +
                ", auc=" + aucRoc +
                ", loss=" + loss +
                ", time=" + executionTimeMs +
                '}';
    }
}