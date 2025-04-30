package com.example.automl_prototype_1.controller;

import com.example.automl_prototype_1.model.ExecutionResult;
import com.example.automl_prototype_1.service.AppStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Import Initializable
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell; // <-- ADDED IMPORT
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResultsViewController implements Initializable {

    // --- FXML Injected Fields ---
    // Ensure these fx:id values EXACTLY match your results_view.fxml file
    @FXML private TableView<ExecutionResult> resultsTable;
    @FXML private TableColumn<ExecutionResult, String> algoCol;
    @FXML private TableColumn<ExecutionResult, Double> accuracyCol;
    @FXML private TableColumn<ExecutionResult, Double> aucCol;
    @FXML private TableColumn<ExecutionResult, Double> lossCol;
    @FXML private TableColumn<ExecutionResult, Long> timeCol;
    @FXML private Button exportButton;
    @FXML private Button saveButton;
    // --- FIX: Match FXML fx:id ---
    @FXML private BarChart<String, Number> resultsChart; // <-- RENAMED to match FXML
    // -----------------------------
    @FXML private CategoryAxis chartXAxis; // fx:id from FXML
    @FXML private NumberAxis chartYAxis; // fx:id from FXML
    @FXML private Label noResultsLabel; // Optional: Label fx:id="noResultsLabel"

    // --- Services and Helpers ---
    private final AppStateService appStateService = AppStateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final DecimalFormat formatter = new DecimalFormat("#.####"); // Format to 4 decimal places

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Results View Initializing...");
        setupTableColumns();
        saveButton.setOnAction(e -> handleSaveJson());
        exportButton.setOnAction(e -> handleExport());
        configureChart(); // Separate method for chart setup
        loadAndDisplayResults(); // Load data last
        System.out.println("Results View Initialized Successfully.");
    }

    private void setupTableColumns() {
        System.out.println("Setting up table columns...");
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        accuracyCol.setCellValueFactory(new PropertyValueFactory<>("accuracy"));
        aucCol.setCellValueFactory(new PropertyValueFactory<>("aucRoc"));
        lossCol.setCellValueFactory(new PropertyValueFactory<>("loss"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("executionTimeMs"));

        // Assign cell factories for formatting
        accuracyCol.setCellFactory(tc -> createDoubleFormattingCell());
        aucCol.setCellFactory(tc -> createDoubleFormattingCell());
        lossCol.setCellFactory(tc -> createDoubleFormattingCell());

        // Set numeric columns to right alignment
        accuracyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        aucCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        lossCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        timeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    /**
     * Creates a TableCell that formats Double values using the predefined formatter.
     * Handles null and NaN values gracefully.
     * @return A configured TableCell for Double values.
     */
    private TableCell<ExecutionResult, Double> createDoubleFormattingCell() {
        // Use lambda expression for concise cell factory creation
        return new TableCell<ExecutionResult, Double>() {
            @Override // Override annotation is correct here
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty); // Always call super
                if (empty || item == null || item.isNaN()) {
                    setText(null); // Display nothing if empty, null, or NaN
                } else {
                    setText(formatter.format(item)); // Format valid numbers
                }
            }
        }; // End of anonymous TableCell class
    }

    /**
     * Configures the basic appearance of the BarChart.
     */
    private void configureChart() {
        resultsChart.setTitle("Algorithm Performance Comparison");
        chartXAxis.setLabel("Algorithm");
        chartYAxis.setLabel("Accuracy"); // Default Y-axis label
        resultsChart.setAnimated(false);
        resultsChart.getData().clear();  // Ensure chart is clear initially
    }

    /**
     * Fetches results from AppStateService and updates the TableView and BarChart.
     */
    private void loadAndDisplayResults() {
        System.out.println("Fetching results from AppStateService...");
        List<ExecutionResult> results = appStateService.getExecutionResults();

        // Clear previous data
        resultsTable.getItems().clear();
        resultsChart.getData().clear(); // Use correct chart variable name

        if (results != null && !results.isEmpty()) {
            System.out.println("Found " + results.size() + " results. Populating UI.");
            ObservableList<ExecutionResult> observableResults = FXCollections.observableArrayList(results);
            resultsTable.setItems(observableResults);
            populateChart(observableResults);

            // Hide the "No results" label/placeholder
            resultsTable.setPlaceholder(null);
            if (noResultsLabel != null) {
                noResultsLabel.setVisible(false);
                noResultsLabel.setManaged(false);
            }

        } else {
            System.out.println("No results found in AppStateService to display.");
            // Show the "No results" label/placeholder
            if (noResultsLabel != null) {
                noResultsLabel.setVisible(true);
                noResultsLabel.setManaged(true);
            } else {
                resultsTable.setPlaceholder(new Label("No results to display. Run a model first."));
            }
        }
    }

    /**
     * Populates the BarChart with Accuracy data from the results.
     * @param results The ObservableList of ExecutionResult objects.
     */
    private void populateChart(ObservableList<ExecutionResult> results) {
        System.out.println("Populating performance chart...");
        chartYAxis.setLabel("Accuracy"); // Set axis label

        XYChart.Series<String, Number> accuracySeries = new XYChart.Series<>();
        accuracySeries.setName("Accuracy");

        for (ExecutionResult result : results) {
            if (!Double.isNaN(result.getAccuracy())) {
                accuracySeries.getData().add(
                        new XYChart.Data<>(result.getAlgorithmName(), result.getAccuracy())
                );
            } else {
                System.out.println("Skipping chart data point for " + result.getAlgorithmName() + " due to NaN accuracy.");
            }
        }

        if (!accuracySeries.getData().isEmpty()) {
            // --- FIX: Use correct chart variable name ---
            resultsChart.getData().add(accuracySeries);
            // -----------------------------------------
            System.out.println("Added accuracy series to chart with " + accuracySeries.getData().size() + " data points.");
        } else {
            System.out.println("No valid accuracy data to display in chart.");
        }
    }

    // --- Action Handlers for Buttons ---

    @FXML
    private void handleSaveJson() {
        // ... (Implementation remains the same) ...
        System.out.println("Save Results (JSON) button clicked.");
        ObservableList<ExecutionResult> results = resultsTable.getItems();
        if (results == null || results.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Save Error", "No results available to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results as JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        fileChooser.setInitialFileName("automl_results_" + System.currentTimeMillis() + ".json");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }
            try {
                objectMapper.writeValue(file, results);
                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Results saved to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save results to file:\n" + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Error during JSON serialization:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        // ... (Implementation remains the same) ...
        System.out.println("Export Report button clicked.");
        ObservableList<ExecutionResult> results = resultsTable.getItems();
        if (results == null || results.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export Error", "No results available to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Results as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("automl_export_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv"); // Corrected extension
            }
            try (FileWriter writer = new FileWriter(file)) {
                List<String> headers = resultsTable.getColumns().stream()
                        .map(TableColumn::getText)
                        .collect(Collectors.toList());
                writer.append(String.join(",", headers));
                writer.append("\n");

                for (ExecutionResult result : results) {
                    writer.append(escapeCsv(result.getAlgorithmName())).append(",");
                    writer.append(formatCsvDouble(result.getAccuracy())).append(",");
                    writer.append(formatCsvDouble(result.getAucRoc())).append(",");
                    writer.append(formatCsvDouble(result.getLoss())).append(",");
                    writer.append(String.valueOf(result.getExecutionTimeMs()));
                    writer.append("\n");
                }
                writer.flush();
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Results exported to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export results to CSV file:\n" + e.getMessage());
            }
        }
    }

    // --- Helper Methods ---

    private String escapeCsv(String value) {
        // ... (Implementation remains the same) ...
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String formatCsvDouble(double value) {
        // ... (Implementation remains the same) ...
        return Double.isNaN(value) ? "" : String.format(Locale.US, "%.4f", value);
    }

    private Stage getStage() {
        // ... (Implementation remains the same) ...
        return (Stage) resultsTable.getScene().getWindow();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        // ... (Implementation remains the same) ...
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Removed SimpleResultDTO as not needed for direct serialization attempt

} // End of ResultsViewController class