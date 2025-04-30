package com.example.automl_prototype_1.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a loaded dataset, storing headers and records (as Maps).
 * Can be initialized with or without a source File reference.
 */
public class Dataset {
    private final File sourceFile; // Can be null if loaded from a stream/reader
    private final List<String> headers;
    private final List<Map<String, String>> records;

    /**
     * Constructor for the Dataset.
     * @param sourceFile The original source file, or null if loaded from another source.
     * @param headers The list of header names (column names).
     * @param records A list of maps, where each map represents a record (row)
     *                with header names as keys and cell values as strings.
     */
    public Dataset(File sourceFile, List<String> headers, List<Map<String, String>> records) {
        this.sourceFile = sourceFile;
        // Ensure headers and records are not null, even if empty
        this.headers = (headers != null) ? new ArrayList<>(headers) : new ArrayList<>();
        this.records = (records != null) ? new ArrayList<>(records) : new ArrayList<>();
    }

    // --- Getters ---

    /**
     * Gets the source file reference. May be null.
     * @return The File object, or null.
     */
    public File getSourceFile() { return sourceFile; }

    /**
     * Gets the list of headers (column names).
     * @return An unmodifiable list of header strings.
     */
    public List<String> getHeaders() {
        // Consider returning Collections.unmodifiableList(headers) for immutability
        return headers;
    }

    /**
     * Gets the records as a list of maps.
     * @return An unmodifiable list where each map represents a row (header -> value).
     */
    public List<Map<String, String>> getRecords() {
        // Consider returning Collections.unmodifiableList(records)
        return records;
    }

    /**
     * Gets the number of data records (rows) in the dataset.
     * @return The count of records.
     */
    public int getRecordCount() { return records.size(); }

    /**
     * Gets the number of features (columns) based on the header count.
     * @return The count of features.
     */
    public int getFeatureCount() { return headers.size(); }


    /**
     * Provides a string representation of the Dataset object,
     * safely handling cases where the source file might be null.
     * @return A string summary of the dataset.
     */
    @Override
    public String toString() {
        // --- FIX: Handle null sourceFile safely ---
        String sourceFileName = (sourceFile != null) ? sourceFile.getName() : "Stream/Reader Input";
        // ------------------------------------------
        return "Dataset{" +
                "source=" + sourceFileName + // Use the null-safe variable
                ", records=" + getRecordCount() +
                ", features=" + getFeatureCount() +
                '}';
    }
}