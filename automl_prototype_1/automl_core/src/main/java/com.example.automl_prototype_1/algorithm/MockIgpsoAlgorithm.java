package com.example.automl_prototype_1.algorithm;

// No need to import Dataset or ExecutionResult here if only overriding abstract methods

/**
 * Mock implementation for Igpso Algorithm.
 */
public class MockIgpsoAlgorithm extends AbstractMockAlgorithm { // Extends the base class

    private static final String ALGO_NAME = "Igpso";
    private static final long MOCK_TIME_MS = 1800; // Example simulation time

    /**
     * Constructor.
     * FIX: Removed super(ALGO_NAME) call as AbstractMockAlgorithm has no such constructor.
     */
    public MockIgpsoAlgorithm() {
        // super(ALGO_NAME); // DELETE or COMMENT OUT this line
        System.out.println(ALGO_NAME + " Mock Initialized");
    }

    /**
     * Provides the specific name for this algorithm.
     * Overrides the abstract method from the parent class.
     */
    @Override
    public String getAlgorithmName() {
        return ALGO_NAME;
    }

    /**
     * Provides the simulated execution time for this algorithm.
     * FIX: Added implementation for the required abstract method.
     */
    @Override
    protected long getMockExecutionTime() {
        return MOCK_TIME_MS;
    }

    // The execute method is inherited from AbstractMockAlgorithm
    // No need to override it here unless specific mock behavior is needed for Igpso
}