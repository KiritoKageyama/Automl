package com.example.automl_prototype_1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class DashboardApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            String fxmlPath = "/com/example/automl_prototype_1/dashboard.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("FATAL: Cannot find FXML file: " + fxmlPath);
                System.exit(1);
            }
            System.out.println("Loading FXML from: " + fxmlUrl);

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root); // Create the scene first

            // --- Load CSS Programmatically ---
            String cssPath = "/com/example/automl_prototype_1/css/dashboard.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                System.err.println("WARNING: Cannot find CSS file: " + cssPath);
                // Application will continue without custom styling
            } else {
                System.out.println("Loading CSS from: " + cssUrl.toExternalForm());
                scene.getStylesheets().add(cssUrl.toExternalForm()); // Add CSS to the scene
            }
            // --------------------------------

            primaryStage.setTitle("AutoML Dashboard");
            primaryStage.setScene(scene); // Set the scene
            primaryStage.show();
            System.out.println("Application started successfully.");

        } catch (Throwable t) {
            System.err.println("Error during application start:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Launching JavaFX application...");
            launch(args);
        } catch (Throwable t) {
            System.err.println("Error during JavaFX launch:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}