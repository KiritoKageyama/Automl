package com.example.automl_prototype_1.dataprovider;

import com.example.automl_prototype_1.model.Dataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

// Removed File-specific imports if no longer needed elsewhere in this file
import java.io.IOException;
import java.io.Reader; // Required for the input parameter
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides functionality to load datasets from CSV sources.
 */
public class CsvDataProvider {

    /**
     * Loads a Dataset from the given Reader.
     * Assumes the CSV data has headers in the first row.
     *
     * @param dataReader The Reader providing the CSV data (e.g., InputStreamReader from Android).
     * @return A Dataset object containing the parsed data.
     * @throws IOException If reading or parsing fails.
     */
    public Dataset loadDataset(Reader dataReader) throws IOException {
        // Check if the provided reader is valid
        if (dataReader == null) {
            throw new IOException("Invalid Reader provided (null). Cannot load dataset.");
        }

        System.out.println("CsvDataProvider: Loading dataset from Reader...");
        List<Map<String, String>> recordsList = new ArrayList<>();
        List<String> headers; // To store header names

        // Configure the CSV format
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader() // Expect headers in the first row
                .setSkipHeaderRecord(true) // Skip reading the header row as data
                .setIgnoreHeaderCase(true) // Ignore case differences in headers
                .setTrim(true) // Trim whitespace from values
                .setIgnoreEmptyLines(true) // Ignore blank lines
                .build();

        // Use try-with-resources to ensure the CSVParser is closed
        // The CSVParser will typically close the underlying Reader it was given
        try (CSVParser csvParser = format.parse(dataReader)) { // CORRECT: Pass the Reader here

            // Get headers after parsing starts
            headers = csvParser.getHeaderNames();
            if (headers == null || headers.isEmpty()) {
                throw new IOException("Could not read headers from CSV reader or source is empty.");
            }
            System.out.println("CsvDataProvider: Headers found: " + headers);

            // Iterate over data records
            for (CSVRecord csvRecord : csvParser) {
                // Convert each record to a Map<String, String> (header -> value)
                recordsList.add(csvRecord.toMap());
            }

        } // csvParser (and likely dataReader) are closed here

        // Log information about loaded data
        if (recordsList.isEmpty() && !headers.isEmpty()) {
            System.out.println("CsvDataProvider: Warning - Loaded headers but no data records found.");
        } else {
            System.out.println("CsvDataProvider: Loaded " + recordsList.size() + " records with " + headers.size() + " features.");
        }

        // Create the Dataset object, passing null for the File reference
        // because we loaded from a Reader.
        return new Dataset(null, headers, recordsList); // CORRECT: Pass null for File
    }

    // Optional: You can keep the File overload if your desktop app still uses it
    /*
    public Dataset loadDataset(java.io.File csvFile) throws IOException {
        if (csvFile == null || !csvFile.exists() || !csvFile.isFile()) {
             throw new IOException("Invalid CSV file provided: " + (csvFile != null ? csvFile.getPath() : "null"));
        }
        System.out.println("CsvDataProvider: Loading dataset from File: " + csvFile.getPath());
        try (Reader fileReader = new java.io.FileReader(csvFile)) { // Create a Reader from the File
             return loadDataset(fileReader); // Delegate to the Reader method
        }
    }
    */
}