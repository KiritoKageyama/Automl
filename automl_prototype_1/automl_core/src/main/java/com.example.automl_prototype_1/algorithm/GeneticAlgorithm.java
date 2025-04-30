package com.example.automl_prototype_1.algorithm;

import com.example.automl_prototype_1.model.Dataset;
import com.example.automl_prototype_1.model.ExecutionResult;
import smile.classification.KNN;        // KNN Classifier
// Note: DataFrame, Formula, vector imports are no longer needed here as we use arrays directly
// import smile.data.DataFrame;
// import smile.data.formula.Formula;
// import smile.data.vector.BaseVector;
// import smile.data.vector.DoubleVector;
// import smile.data.type.StructField;
import smile.validation.metric.Accuracy; // Classification metric

// Import necessary standard Java classes
import java.util.*;
import java.util.stream.IntStream;

/**
 * Implementation of a Genetic Algorithm focused on optimizing the 'k'
 * hyperparameter for a KNN classifier using the Smile 2.6.0 library.
 * Uses direct array manipulation after preprocessing.
 */
public class GeneticAlgorithm implements OptimizationAlgorithm {

    private static final String ALGORITHM_NAME = "Genetic Algorithm (KNN HyperOpt)";
    private final Random random = new Random();

    // --- GA Configuration Parameters ---
    private int populationSize = 20;
    private int maxGenerations = 30;
    private double crossoverRate = 0.8;
    private double mutationRate = 0.1;
    private boolean elitism = true;
    private int tournamentSize = 5;

    // --- Hyperparameter Space (KNN 'k') ---
    private int minK = 1;
    private int maxK = 20;

    @Override
    public String getAlgorithmName() { return ALGORITHM_NAME; }

    @Override
    public ExecutionResult execute(Dataset rawDataset, Map<String, Object> configuration) {
        System.out.println("--- Executing GA for KNN Hyperparameter Optimization ---");
        long startTime = System.currentTimeMillis();

        applyConfiguration(configuration);

        // --- 1. Preprocessing ---
        // Converts raw Map data directly to feature/target arrays
        ProcessedData processedData = preprocessDataBasic(rawDataset);
        if (processedData == null || processedData.x == null || processedData.y == null || processedData.x.length == 0) {
            throw new RuntimeException("Fatal: Preprocessing failed to produce valid data arrays.");
        }
        System.out.println("Preprocessing returned: x["+processedData.x.length+"]["+(processedData.x.length > 0 ? processedData.x[0].length : 0)+"], y["+processedData.y.length+"]");
        // ------------------------

        // --- 2. Train/Test Split ---
        System.out.println("Splitting data (70% train, 30% test)...");
        int n = processedData.y.length;
        int[][] splitIndices = trainTestSplitManual(n, 0.7, random); // Use manual split helper
        int[] trainIndices = splitIndices[0];
        int[] testIndices = splitIndices[1];
        if (trainIndices.length == 0 || testIndices.length == 0) {
            throw new RuntimeException("Fatal: Dataset too small or split percentage invalid.");
        }

        // Create training and testing arrays directly from ProcessedData
        double[][] xTrain = selectRows(processedData.x, trainIndices);
        int[] yTrain = selectElementsInt(processedData.y, trainIndices); // Use int[] y
        double[][] xTest = selectRows(processedData.x, testIndices);
        int[] yTest = selectElementsInt(processedData.y, testIndices);   // Use int[] y
        System.out.println("Train samples: " + xTrain.length + ", Test samples: " + xTest.length);
        // --------------------------

        // 3. Initialize Population
        List<Solution> population = initializePopulation(xTrain.length); // Pass train size for k clamping

        Solution bestOverallSolution = null;

        // 4. Evolution Loop
        System.out.println("Starting GA generations...");
        for (int generation = 0; generation < maxGenerations; generation++) {
            // 5. Evaluate Fitness
            evaluatePopulation(population, xTrain, yTrain, xTest, yTest); // Pass arrays

            Solution bestOfGeneration = findBestSolution(population);
            if (bestOverallSolution == null || bestOfGeneration.fitness > bestOverallSolution.fitness) {
                bestOverallSolution = new Solution(bestOfGeneration); // Use copy constructor
            }

            // Log progress
            if (generation == 0 || (generation + 1) % 5 == 0 || generation == maxGenerations - 1) {
                System.out.printf("GA Gen %d/%d: Best Test Accuracy = %.4f, Best K = %d%n",
                        generation + 1, maxGenerations, bestOfGeneration.fitness, bestOfGeneration.k);
            }

            // 6. Prepare Next Generation
            List<Solution> nextPopulation = createNextGeneration(population, bestOverallSolution);
            population = nextPopulation;

        } // End generations loop

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 7. Report Final Result
        if (bestOverallSolution != null) {
            System.out.printf("GA Finished. Best K = %d, Best Test Accuracy = %.4f%n",
                    bestOverallSolution.k, bestOverallSolution.fitness);
            return new ExecutionResult(ALGORITHM_NAME + " (Best K=" + bestOverallSolution.k + ")",
                    round(bestOverallSolution.fitness, 4), Double.NaN, Double.NaN, duration);
        } else {
            System.err.println("GA Finished. No best solution found.");
            return new ExecutionResult(ALGORITHM_NAME, Double.NaN, Double.NaN, Double.NaN, duration);
        }
    }


    // --- Helper Class to hold preprocessed data ---
    private static class ProcessedData {
        final double[][] x; // Features
        final int[] y;      // Target as int[]
        ProcessedData(double[][] x, int[] y) { this.x = x; this.y = y; }
    }
    // --------------------------------------------


    // --- GA Methods ---

    private void applyConfiguration(Map<String, Object> config) { /* ... keep placeholder ... */ }

    /** Basic Preprocessing: Converts raw Map data to double[][] features and int[] target */
    private ProcessedData preprocessDataBasic(Dataset rawDataset) {
        try {
            List<String> headers = rawDataset.getHeaders();
            List<Map<String, String>> records = rawDataset.getRecords();
            if (headers == null || records == null || headers.isEmpty() || records.isEmpty()) {
                System.err.println("Cannot preprocess empty dataset."); return null;
            }

            int numRecords = records.size();
            int numCols = headers.size();
            if (numCols < 2) {
                System.err.println("Dataset must have at least 2 columns (features + target)."); return null;
            }
            int featureCols = numCols - 1;
            int targetColIndex = numCols - 1; // Still assuming last col is target - needs config

            double[][] features = new double[numRecords][featureCols];
            int[] target = new int[numRecords]; // Target as int[]

            System.out.println("Preprocessing " + numRecords + " records...");

            for (int i = 0; i < numRecords; i++) {
                Map<String, String> record = records.get(i);
                if (record == null || record.size() != numCols) { // Added check for record integrity
                    System.err.printf("Warning: Row %d is null or has wrong column count (%d vs %d). Skipping.%n",
                            i + 1, (record == null ? 0 : record.size()), numCols);
                    // Consider how to handle target if row is skipped - might need to resize arrays later or use NaN/default
                    if(i < target.length) target[i] = -1; // Mark target as invalid? Or use a specific value.
                    continue; // Skip this row
                }

                int currentFeatureIndex = 0;
                for (int j = 0; j < numCols; j++) {
                    String header = headers.get(j);
                    // Use getOrDefault in case a column is missing in a specific record map
                    String val = record.getOrDefault(header, "");
                    double numVal = 0.0; // Default for errors/missing

                    try {
                        numVal = (val == null || val.isBlank()) ? 0.0 : Double.parseDouble(val);
                    } catch (NumberFormatException e) {
                        System.err.printf("Warning: Row %d, Col '%s'. Could not parse '%s'. Using 0.0%n", i + 1, header, val);
                        // Consider using NaN if your models/fitness can handle it: numVal = Double.NaN;
                    }

                    if (j == targetColIndex) {
                        target[i] = (int) Math.round(numVal); // Convert target to int
                    } else {
                        if (currentFeatureIndex < featureCols) {
                            features[i][currentFeatureIndex++] = numVal;
                        } else {
                            System.err.printf("Logic Error: Row %d, Col '%s'. Feature index out of bounds.%n", i + 1, header);
                        }
                    }
                }
                // This check might be less useful now if we skip rows above
                // if (currentFeatureIndex != featureCols) { ... }
            }
            System.out.println("Preprocessing complete.");
            // TODO: Handle skipped rows if necessary (e.g., create smaller arrays)
            return new ProcessedData(features, target);

        } catch (Exception e) {
            System.err.println("Error during basic preprocessing:");
            e.printStackTrace();
            return null;
        }
    }


    /** Evaluate fitness using train/test split */
    // Signature uses int[] for target arrays
    private void evaluatePopulation(List<Solution> population, double[][] xTrain, int[] yTrain, double[][] xTest, int[] yTest) {
        for (Solution sol : population) {
            int kValue = 0;
            try {
                kValue = sol.getClampedK(xTrain.length);

                // KNN.fit takes int[] target yTrain
                KNN<double[]> knn = KNN.fit(xTrain, yTrain, kValue);

                // knn.predict takes double[][] features xTest and returns int[] predictions
                int[] predictions = knn.predict(xTest);

                // Accuracy compares int[] yTest vs int[] predictions
                sol.fitness = Accuracy.of(yTest, predictions);

            } catch (IllegalArgumentException e) {
                System.err.println("Error evaluating fitness for k=" + sol.k + " (clamped: " + kValue + "): " + e.getMessage());
                sol.fitness = Double.NEGATIVE_INFINITY; // Give very bad fitness
            } catch (Exception e) { // Catch other unexpected errors
                System.err.println("Unexpected error evaluating fitness for k=" + sol.k + " (clamped: " + kValue + ")");
                e.printStackTrace();
                sol.fitness = Double.NEGATIVE_INFINITY; // Penalize heavily
            }
        }
    }


    /** Initialize population */
    private List<Solution> initializePopulation(int maxPossibleK) {
        int effectiveMaxK = Math.min(this.maxK, maxPossibleK > 0 ? maxPossibleK - 1 : this.maxK);
        effectiveMaxK = Math.max(minK, effectiveMaxK);
        int effectiveMinK = Math.min(minK, effectiveMaxK);

        System.out.println("GA Initializing population (KNN k) of size " + populationSize
                + " with k range [" + effectiveMinK + ", " + effectiveMaxK + "]");
        List<Solution> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            // Call the constructor taking two ints
            population.add(new Solution(effectiveMinK, effectiveMaxK));
        }
        return population;
    }

    /** Creates the next generation */
    private List<Solution> createNextGeneration(List<Solution> currentPopulation, Solution bestOverall) {
        List<Solution> nextPopulation = new ArrayList<>(populationSize);
        if (elitism && bestOverall != null) {
            // Use copy constructor
            nextPopulation.add(new Solution(bestOverall));
        }
        List<Solution> parents = selection(currentPopulation);
        while (nextPopulation.size() < populationSize) {
            Solution parent1 = parents.get(random.nextInt(parents.size()));
            Solution parent2 = parents.get(random.nextInt(parents.size()));
            // Use copy constructor
            Solution child1 = new Solution(parent1);
            Solution child2 = new Solution(parent2);
            if (random.nextDouble() < crossoverRate) { performCrossover(child1, child2); }
            mutate(child1);
            mutate(child2);
            if (nextPopulation.size() < populationSize) nextPopulation.add(child1);
            if (nextPopulation.size() < populationSize) nextPopulation.add(child2);
        }
        return nextPopulation;
    }

    // --- findBestSolution, selection, performCrossover, mutate (Keep implementations) ---
    private Solution findBestSolution(List<Solution> population) { return Collections.max(population, Comparator.comparingDouble(s -> s.fitness)); }
    private List<Solution> selection(List<Solution> population) { /* ... Tournament selection ... */
        List<Solution> parents = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            Solution bestInTournament = population.get(random.nextInt(population.size()));
            for (int j = 1; j < tournamentSize; j++) {
                Solution competitor = population.get(random.nextInt(population.size()));
                if (competitor.fitness > bestInTournament.fitness) bestInTournament = competitor;
            }
            parents.add(bestInTournament);
        }
        return parents;
    }
    private void performCrossover(Solution child1, Solution child2) { /* ... Averaging crossover ... */
        int k1 = child1.k; int k2 = child2.k;
        child1.k = (k1 + k2) / 2; child2.k = (k1 + k2 + 1) / 2;
        child1.clampK(); child2.clampK();
    }
    private void mutate(Solution solution) { /* ... Small change mutation ... */
        if (random.nextDouble() < mutationRate) {
            int change = random.nextInt(3) - 1;
            if (change != 0) { solution.k += change; solution.clampK(); }
        }
    }

    // --- Solution Class (Ensure ONLY these two constructors) ---
    private class Solution {
        int k;
        double fitness = Double.NEGATIVE_INFINITY;
        final int currentMinK;
        final int currentMaxK;

        // Constructor for initialization
        public Solution(int minPossibleK, int maxPossibleK) {
            this.currentMinK = Math.max(1, minPossibleK);
            this.currentMaxK = Math.max(this.currentMinK, maxPossibleK);
            this.k = (this.currentMaxK >= this.currentMinK) ?
                    random.nextInt(this.currentMaxK - this.currentMinK + 1) + this.currentMinK
                    : this.currentMinK;
        }

        // Copy constructor
        public Solution(Solution other) {
            this.k = other.k;
            this.fitness = other.fitness;
            this.currentMinK = other.currentMinK;
            this.currentMaxK = other.currentMaxK;
        }

        // No other constructors should be present

        void clampK() { this.k = Math.max(this.currentMinK, Math.min(this.k, this.currentMaxK)); }
        int getClampedK(int trainingSetSize) {
            int maxAllowedByData = Math.max(1, trainingSetSize - 1); // k must be < N
            return Math.max(this.currentMinK, Math.min(this.k, Math.min(this.currentMaxK, maxAllowedByData)));
        }
    }
    // --- End Solution Class ---

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    // --- Manual Train/Test Split ---
    private static int[][] trainTestSplitManual(int n, double ratio, Random random) {
        int[] indices = IntStream.range(0, n).toArray();
        for (int i = n - 1; i > 0; i--) { int j = random.nextInt(i + 1); int temp = indices[i]; indices[i] = indices[j]; indices[j] = temp; }
        int trainSize = (int) (n * ratio);
        int[] train = Arrays.copyOfRange(indices, 0, trainSize);
        int[] test = Arrays.copyOfRange(indices, trainSize, n);
        return new int[][] { train, test };
    }
    private static double[][] selectRows(double[][] data, int[] indices) {
        double[][] subset = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) { subset[i] = data[indices[i]]; }
        return subset;
    }
    // Helper to select elements from int[]
    private static int[] selectElementsInt(int[] data, int[] indices) {
        int[] subset = new int[indices.length];
        for (int i = 0; i < indices.length; i++) { subset[i] = data[indices[i]]; }
        return subset;
    }
    // -----------------------------

} // End of GeneticAlgorithm class