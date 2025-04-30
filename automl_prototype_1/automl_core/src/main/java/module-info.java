/**
 * Module descriptor for the core AutoML logic.
 */
// Use the actual groupId and artifactId from automl-core/pom.xml
// Example: if groupId=com.shadowtengu, artifactId=automl-core
module com.shadowtengu.automl_core {

    // --- Dependencies Required by Core Logic ---
    requires org.apache.commons.csv;
    requires smile.core;
    requires smile.data;
    requires smile.io;
    requires smile.math; // Keep this for Smile 2.6.0

    // Jackson required because we open the model package to it
    requires com.fasterxml.jackson.databind;

    // Optional: requires slf4j.api;


    // --- Packages EXPORTED for Use by Other Modules ---
    // Make the core functionality available to desktop-app and android-app
    exports com.example.automl_prototype_1.model;
    exports com.example.automl_prototype_1.service;
    exports com.example.automl_prototype_1.dataprovider;
    exports com.example.automl_prototype_1.algorithm;


    // --- Open Packages needed for Reflection by External Modules ---
    // Allow Jackson (running in desktop-app) to access model classes via reflection
    opens com.example.automl_prototype_1.model to com.fasterxml.jackson.databind;

    // --- REMOVE JavaFX specific directives ---
    // REMOVE: opens com.example.automl_prototype_1.controller to javafx.fxml;
    // REMOVE: exports com.example.automl_prototype_1 to javafx.graphics;

}