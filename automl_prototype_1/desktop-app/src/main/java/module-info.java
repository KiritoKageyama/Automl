module com.example.automl_prototype_1 { // Make sure the module name here is correct

    // --- Existing requires ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.web; // Keep if needed by ControlsFX or other deps

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx; // Assuming artifactId maps to this module name
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx; // Assumes this is the module name
    requires eu.hansolo.fx.countries; // Might be needed explicitly if tilesfx doesn't require transitive
    requires eu.hansolo.fx.heatmap;   // Might be needed explicitly if tilesfx doesn't require transitive
    requires eu.hansolo.toolboxfx;    // Might be needed explicitly if tilesfx doesn't require transitive
    requires eu.hansolo.toolbox;      // Might be needed explicitly if tilesfx doesn't require transitive
    requires com.almasb.fxgl.all; // Or specific FXGL modules if not using 'all'
    // requires org.apache.commons.csv;
    // smile
    // requires smile.core;
    // requires smile.data;
    // requires smile.io;
    // requires smile.math;
    requires com.fasterxml.jackson.databind;
    // requires smile.classification;
    //requires smile.xgboost;// Add if using XGBoost
    // --- Add these exports ---
    // Export the controller package TO javafx.fxml so FXMLLoader can access it
    opens com.example.automl_prototype_1.controller to javafx.fxml;

    // Export the main application package TO javafx.graphics so Application.launch can access it
    exports com.example.automl_prototype_1 to javafx.graphics;

    // Note: If you use @FXML injection for private fields in your controller,
    // you might eventually need 'opens' instead of 'exports' for the controller package:
    // opens com.example.automl_prototype_1.controller to javafx.fxml;
    // But start with 'exports' first.
    // --- *** ADD Requires for the Core Module *** ---
    // Use the actual groupId.artifactId defined in automl-core/pom.xml
    // Assuming groupId="com.shadowtengu", artifactId="automl-core"
    requires com.shadowtengu.automl_core ;
    // Allow Jackson access to the *core model* classes FOR serialization
    // This assumes Jackson is used *within* the desktop-app (e.g., ResultsViewController)
    // to serialize objects fetched from the core service.

}