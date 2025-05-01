# AutoML Prototype Project

## 1. Introduction / Problem Description

This project is a prototype system designed to explore, execute, and compare optimization algorithms (like Genetic Algorithms, PSO variants) for the purpose of tuning hyperparameters of base machine learning models (initially focusing on KNN via the Smile ML library).

The core problem addressed is providing a foundational, cross-platform tool for users interested in applying metaheuristic optimization techniques to common machine learning tasks without needing deep expertise in every underlying algorithm. The system aims to simplify the workflow from data input to viewing comparative performance results.

This repository contains the complete, ongoing development for this project.

## 2. Key Features and Functionalities

*   **Cross-Platform:** Provides both a Desktop GUI (JavaFX) and a native Android GUI (Jetpack Compose).
*   **Modular Design:** A central Java library (`automl_core`) contains reusable logic, separate from the UIs.
*   **CSV Dataset Upload:** Allows users to select and load datasets in CSV format via native file choosers.
*   **Algorithm Selection:** Enables users to choose one or more optimization algorithms (currently includes mock implementations and a basic Genetic Algorithm structure).
*   **Base Model Selection:** (Desktop) Allows selection of the base ML model (e.g., KNN).
*   **Execution Engine:** Executes the selected optimization algorithms on the loaded data using the core library logic.
*   **Results Visualization:** Displays key performance metrics (Accuracy, AUC-ROC, Loss, Time) in a table and a basic bar chart for comparison.
*   **Background Processing:** Utilizes background tasks (JavaFX Task, Kotlin Coroutines) to keep the UI responsive.
*   **(Planned)** Results saving (JSON) and report exporting.

## 3. Target Audience

*   Students learning Object-Oriented Programming, Java, Machine Learning concepts, and software design principles.
*   Researchers or practitioners needing a prototype platform to test or compare different hyperparameter optimization algorithms.
*   Developers looking for a foundational example of integrating a Java core library with different UI frameworks (JavaFX and Android Compose).

## 4. Technologies Used & Package Dependencies

*   **Core Logic (`automl_core`):**
    *   **Language:** Java 17+
    *   **Build:** Apache Maven
    *   **Modularity:** Java Platform Module System (JPMS - `module-info.java`)
    *   **ML:** Smile ML Library (v2.6.0)
    *   **CSV Parsing:** Apache Commons CSV
*   **Desktop UI (`desktop-app`):**
    *   **Language:** Java 17+
    *   **Framework:** JavaFX 21+ (FXML, Controls, Charts)
    *   **Build:** Apache Maven
    *   **JSON:** Jackson Databind
*   **Android UI (`android-app`):**
    *   **Language:** Kotlin (v1.9.x)
    *   **Build:** Gradle (with Kotlin DSL - `.kts`)
    *   **UI:** Jetpack Compose (Material 3, Navigation, ViewModel v2.7.0, Lifecycle)
    *   **APIs:** Android SDK (Target/Compile 34), Activity Result API
    *   **Concurrency:** Kotlin Coroutines
*   **Development:**
    *   **IDE:** IntelliJ IDEA / Android Studio
    *   **Version Control:** Git / GitHub

*   **Major External Dependencies:** See `pom.xml` files (in root and submodules) and `AutoMLAndroidApp/app/build.gradle.kts`.

## 5. Project Structure

This repository follows a monorepo structure containing multiple related projects:

*   **`automl_prototype_1/`**: Contains the core Java library and the desktop application.
    *   **`automl_core/`**: The platform-independent Java library (Maven project).
    *   **`desktop-app/`**: The JavaFX desktop GUI (Maven project).
    *   `pom.xml`: Parent Maven configuration.
*   **`AutoMLAndroidApp/`**: The native Android application (Gradle project).
*   `.gitignore`: Root ignore file for common temporary/build files.
*   `README.md`: This file.

## 6. Steps to Execute the Project

**Prerequisites:**

*   JDK 17 or higher (`JAVA_HOME` set).
*   Apache Maven.
*   Android Studio (includes Android SDK).
*   Git.

**Setup & Build:**

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/KiritoKageyama/Automl.git
    cd Automl
    ```
2.  **Build `automl_core`:** Navigate to the parent directory and run Maven install. This places the `automl_core.jar` in your local Maven repository (`~/.m2/repository`).
    ```bash
    cd automl_prototype_1
    mvn clean install
    cd ..
    ```
    *(Ensure this build is successful.)*

**Running the Desktop App:**

1.  **Navigate:**
    ```bash
    cd automl_prototype_1/desktop-app
    ```
2.  **Run:**
    ```bash
    mvn javafx:run
    ```

**Running the Android App:**

1.  **Open Project:** Open the `AutoMLAndroidApp` directory specifically in Android Studio.
2.  **Gradle Sync:** Allow Gradle to sync. It should find `automl_core` via `mavenLocal()`.
3.  **Select Device/Emulator:** Choose a target (API 26+).
4.  **Run:** Click the "Run 'app'" button (▶️) in Android Studio.

## 7. SDLC & Development Notes

*   **SDLC:** Developed using an **Iterative and Incremental** model.
*   **Requirements:** Based on project goals and academic requirements (OOP principles). Evolved based on technical feasibility discovered during development.
*   **Testing:** Primarily Manual Functional Testing and extensive logging (Logcat/System.out). Formal Unit/Integration testing is part of future scope.
*   **Key Challenges & Solutions:** Addressed Android build configurations (PKIX, mavenLocal, SDK/Kotlin versions), Android `Uri` vs. Core `File` integration (via temp file workaround and core class null-safety fixes), UI rendering issues (deleting placeholder conflicts), core library build errors (Java syntax/logic corrections), and Desktop JSON/state issues. See project documentation/presentation for full details.

## 8. Deployment

This project is currently intended for local development and execution following the steps above.

## 9. Future Scope

*   Implement functional Smile ML algorithms.
*   Add algorithm parameter configuration UI.
*   Implement data preprocessing.
*   Enhance error handling.
*   Add Unit/Integration tests.
*   Improve results visualization.
*   Implement persistent storage.
*   Refactor state management (Dependency Injection).
*   Complete Desktop export/save features.

## 10. Author

*   Arin Bansal 

---
