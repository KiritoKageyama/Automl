package com.example.automl_prototype_1.algorithm;

import com.example.automl_prototype_1.model.Dataset;
import com.example.automl_prototype_1.model.ExecutionResult;

import java.util.Map; // For configuration later

// Interface for all optimization algorithms
public interface OptimizationAlgorithm {

    String getAlgorithmName();

    /**
     * Executes the algorithm on the given dataset.
     * @param dataset The input dataset.
     * @param configuration Algorithm-specific configuration (optional for now).
     * @return An ExecutionResult object containing performance metrics.
     */
    ExecutionResult execute(Dataset dataset, Map<String, Object> configuration);
}