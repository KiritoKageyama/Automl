package com.example.automl_prototype_1.controller;

import com.example.automl_prototype_1.service.AppStateService; // <-- IMPORT SERVICE
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class UploadViewController {

    @FXML private Button browseButton;
    @FXML private Label selectedFileLabel;

    // No need to store the file locally anymore, use the service
    // private File selectedFile = null;

    @FXML
    private void initialize() {
        System.out.println("Upload View Initialized");
        browseButton.setOnAction(event -> handleBrowse());

        // Optional: Initialize label based on current state in service when view loads
        File currentFile = AppStateService.getInstance().getSelectedDatasetFile();
        if (currentFile != null) {
            selectedFileLabel.setText("Selected: " + currentFile.getName());
        } else {
            selectedFileLabel.setText("No file selected.");
        }
    }

    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Dataset File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Dataset Files (CSV, XLS, XLSX, ZIP)", "*.csv", "*.xls", "*.xlsx", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        AppStateService stateService = AppStateService.getInstance(); // Get service instance

        if (file != null) {
            // Update the shared state via the service
            stateService.setSelectedDatasetFile(file);
            selectedFileLabel.setText("Selected: " + file.getName());
            // System.out.println("Selected file in Upload View: " + file.getAbsolutePath()); // Logged by service now
        } else {
            // Optional: Clear state if selection is cancelled
            // stateService.setSelectedDatasetFile(null);
            // selectedFileLabel.setText("No file selected.");
            System.out.println("File selection cancelled.");
        }
    }

    // Getter is no longer strictly needed here if DashboardController uses the service
    // public File getSelectedFile() { ... }
}