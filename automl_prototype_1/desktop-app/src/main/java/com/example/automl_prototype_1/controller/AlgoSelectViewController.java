package com.example.automl_prototype_1.controller;

import com.example.automl_prototype_1.service.AppStateService;
import javafx.collections.FXCollections; // Import
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox; // Import

import java.util.List;
import java.util.stream.Collectors;

public class AlgoSelectViewController {

    // --- Optimization Algorithm Checkboxes ---
    @FXML private CheckBox checkGA;
    @FXML private CheckBox checkIGPSO;
    @FXML private CheckBox checkWWO;
    @FXML private CheckBox checkBPSO;
    @FXML private CheckBox checkASO;
    @FXML private CheckBox checkNNP;
    private List<CheckBox> optimizationAlgorithmCheckBoxes;

    // --- Base ML Model Selection ---
    @FXML private ChoiceBox<String> baseModelChoiceBox; // Add ChoiceBox
    private final List<String> availableBaseModels = List.of(
            "KNN", "Random Forest", "SVM", "XGBoost" // Add more as implemented
    );

    @FXML
    private void initialize() {
        System.out.println("Algorithm Selection View Initialized");

        // --- Setup Optimization Algo Checkboxes ---
        optimizationAlgorithmCheckBoxes = List.of(checkGA, checkIGPSO, checkWWO, checkBPSO, checkASO, checkNNP);
        List<String> currentOptAlgos = AppStateService.getInstance().getSelectedAlgorithmNames();
        optimizationAlgorithmCheckBoxes.forEach(cb -> {
            cb.setSelected(currentOptAlgos.contains(cb.getText()));
            cb.setOnAction(this::handleOptCheckboxAction); // Listener for optimization algos
        });

        // --- Setup Base Model ChoiceBox ---
        baseModelChoiceBox.setItems(FXCollections.observableArrayList(availableBaseModels));
        baseModelChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleBaseModelSelection(newValue)
        );
        // Set initial selection from service
        String currentBaseModel = AppStateService.getInstance().getSelectedBaseModelName();
        if (currentBaseModel != null && availableBaseModels.contains(currentBaseModel)) {
            baseModelChoiceBox.setValue(currentBaseModel);
        } else if (!availableBaseModels.isEmpty()) {
            baseModelChoiceBox.setValue(availableBaseModels.get(0)); // Default to first
            handleBaseModelSelection(availableBaseModels.get(0)); // Update service with default
        }
        // -----------------------------------
    }

    // Listener for Optimization algorithm checkboxes
    private void handleOptCheckboxAction(ActionEvent event) {
        updateOptimizationServiceState();
    }

    // Listener for Base Model ChoiceBox
    private void handleBaseModelSelection(String selectedModel) {
        if (selectedModel != null) {
            AppStateService.getInstance().setSelectedBaseModelName(selectedModel);
        }
    }

    // Update service with selected OPTIMIZATION algorithms
    private void updateOptimizationServiceState() {
        if (optimizationAlgorithmCheckBoxes == null) return;
        List<String> selectedNames = optimizationAlgorithmCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
        AppStateService.getInstance().setSelectedAlgorithmNames(selectedNames);
    }

    // Getter - less critical now but might be useful
    public List<String> getSelectedOptimizationAlgorithms() {
        return AppStateService.getInstance().getSelectedAlgorithmNames();
    }
    public String getSelectedBaseModel() {
        return AppStateService.getInstance().getSelectedBaseModelName();
    }
}