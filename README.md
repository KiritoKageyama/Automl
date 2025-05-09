# AutoML Prototype (OOPS Project - B1-B1-B7)

## 1. Project Introduction: An Object-Oriented Approach to AutoML

This project, developed for an Object-Oriented Programming course, is a prototype system for exploring automated machine learning (AutoML) tasks. It focuses on tuning hyperparameters of machine learning models (e.g., KNN using the Smile ML library) via optimization algorithms (like Genetic Algorithms).

The core objective was to design and implement a system applying fundamental **Object-Oriented Programming principles** – encapsulation, inheritance, polymorphism, and abstraction – to create a modular, reusable, and understandable codebase. The project features a central Java library (`automl_core`) embodying these OOP concepts, with two distinct UI frontends (JavaFX Desktop and Android Jetpack Compose) demonstrating its use.

## 2. Key OOP Concepts & Features Demonstrated

*   **Encapsulation:**
    *   Data and behavior are bundled within classes (e.g., `Dataset` managing records and headers, `ExecutionResult` holding metrics, service classes like `ExecutionService` encapsulating business logic).
    *   Access modifiers (`private`, `public`, `protected`) are used to control visibility and protect internal state.
*   **Inheritance:**
    *   `AbstractMockAlgorithm` serves as a base class, with concrete mock algorithms (`MockAsoAlgorithm`, `MockIgpsoAlgorithm`, etc.) extending it to inherit common functionality and provide specific implementations.
*   **Polymorphism:**
    *   The `OptimizationAlgorithm` interface defines a common contract (`execute` method).
    *   `ExecutionService` interacts with different algorithm objects through this interface, invoking the appropriate `execute` behavior based on the runtime type of the object.
*   **Abstraction:**
    *   Interfaces (`OptimizationAlgorithm`) and abstract classes (`AbstractMockAlgorithm`) hide complex implementation details, providing a simplified view to the client code (e.g., `ExecutionService`).
    *   Service classes (`ExecutionService`, `CsvDataProvider`, `AppStateService`) abstract away the complexities of their respective domains.
*   **Classes and Objects:** The entire system is built upon well-defined classes, instantiated as objects to represent entities and manage operations.
*   **Interfaces:** Beyond custom interfaces, standard Java interfaces like `List`, `Map`, and `Reader` are extensively used.
*   **Packages:** The codebase is organized into logical packages (`model`, `service`, `dataprovider`, `algorithm` in `automl_core`; `ui.screens`, `ui.vm` in Android) promoting modularity and preventing naming conflicts.
*   **Exception Handling:** `try-catch` blocks are used to manage potential runtime errors like `IOException` (file operations) and custom exceptions, ensuring program robustness.
*   **Generics:** Utilized with the Java Collections Framework (`List<ExecutionResult>`, `Map<String, String>`) for type safety and code reusability.
*   **Collections Framework:** `ArrayList`, `List`, `Map` are fundamental for storing and managing data structures like datasets, algorithm lists, and results.

## 3. Project Structure: A Modular OOP Design

The project's architecture emphasizes OOP's modularity:

*   **`automl_prototype_1/automl_core/`**: The heart of the project, a platform-independent Java library showcasing OOP design. It contains:
    *   **`model`**: Defines data structures (POJOs) like `Dataset` and `ExecutionResult`.
    *   **`dataprovider`**: `CsvDataProvider` for loading data, encapsulating CSV parsing logic.
    *   **`service`**: `ExecutionService` (orchestrates workflow) and `AppStateService` (simple state management).
    *   **`algorithm`**: `OptimizationAlgorithm` interface and its concrete (mock/basic) implementations.
*   **`automl_prototype_1/desktop-app/`**: A JavaFX desktop GUI that acts as a client to `automl_core`. Demonstrates how a UI layer can interact with the OOP-designed core.
*   **`AutoMLAndroidApp/`**: An Android application (Kotlin & Jetpack Compose) that also acts as a client to `automl_core`, showcasing the core's reusability across different platforms and programming paradigms (OOP Java core with a more functional/declarative Kotlin UI).

## 4. High-Level Workflow (Demonstrating Object Interaction)

1.  **Data Input (UI):** User selects a CSV file.
2.  **Data Encapsulation (UI ViewModel -> Core):**
    *   Android: `UploadViewModel` copies the file to a temporary `File`.
    *   Desktop: `UploadViewController` gets a `File` object.
3.  **Data Loading (Core):**
    *   `CsvDataProvider` is instantiated. Its `loadDataset(File)` method is called.
    *   A `Dataset` object is created, encapsulating headers and records.
4.  **State Management (Core Service):** The reference to the `File` (Desktop/Android) or the `Dataset` object (if core was modified) is stored in the `AppStateService` singleton.
5.  **Algorithm Selection (UI):** User selects algorithms.
6.  **Execution (UI ViewModel -> Core Service):**
    *   The `ExecutionService` is called.
    *   It retrieves the `Dataset` (or loads it from the `File` reference).
    *   It instantiates selected `OptimizationAlgorithm` objects (polymorphism).
    *   It calls the `execute` method on each algorithm object.
7.  **Results (Core Model -> UI):**
    *   Each algorithm returns an `ExecutionResult` object.
    *   `ExecutionService` collects these into a `List<ExecutionResult>`.
    *   This list is stored in `AppStateService`.
8.  **Display (UI):** The Results UI fetches the list from `AppStateService` and displays the metrics.

## 5. Technologies Used (Relevant to OOP & Java)

*   **Language:** Java 17+ (for `automl_core` and `desktop-app`), Kotlin (for `android-app`, interoperable with Java).
*   **Core Java Features:** Classes, Objects, Inheritance, Interfaces, Abstract Classes, Packages, Access Modifiers, Constructors, `this`/`super` keywords, Exception Handling, I/O Streams, Generics, Collections Framework.
*   **Build Systems:** Apache Maven (for Java projects), Gradle (for Android/Kotlin).
*   **Modularity:** Java Platform Module System (JPMS) used in `automl_core` and `desktop-app`.
*   **External Libraries (interacting with core Java objects):** Apache Commons CSV, Smile ML.

## 6. Steps to Execute

*(This section can remain largely the same as the previous README, as it's about running the project.)*

**Prerequisites:**

*   JDK 17+, Maven, Android Studio, Git.

**Setup & Build:**

1.  Clone: `git clone https://github.com/KiritoKageyama/Automl.git && cd Automl`
2.  Build Core: `cd automl_prototype_1 && mvn clean install && cd ..`

**Running Desktop App:**

1.  `cd automl_prototype_1/desktop-app`
2.  `mvn javafx:run`

**Running Android App:**

1.  Open `AutoMLAndroidApp` in Android Studio.
2.  Sync Gradle, select device/emulator, Run 'app'.

## 7. SDLC & Requirements (Focus on OOP Learning)

*   **SDLC:** Iterative and Incremental model allowed for progressive application and refinement of OOP concepts.
*   **Requirements:** Driven by the OOPS course syllabus to demonstrate understanding and application of core Java OOP features. The project evolved to tackle real-world complexities like cross-platform integration while adhering to these OOP tenets.

## 8. Challenges & OOP Solutions

*   **Cross-Platform Data Handling:** The challenge of Android's `Uri` vs. Java's `File` was managed by encapsulating file copying logic in the Android `UploadViewModel` while ensuring the core `CsvDataProvider` and `Dataset` could still function based on `File` objects, preserving the core library's design integrity. The `Dataset` model was made robust to handle a potentially `null` source file reference.
*   **Algorithm Variability:** The `OptimizationAlgorithm` interface and `AbstractMockAlgorithm` class allowed for different algorithms to be treated uniformly (`execute` method) by the `ExecutionService` (polymorphism and abstraction).
*   **State Management:** The `AppStateService` (Singleton pattern) provided a simple way to share state (like the dataset file reference or results) between different parts of the application (different UI controllers/ViewModels).

## 9. Future Scope (From an OOP Perspective)

*   **Refactor to Design Patterns:** More explicit use of patterns like Factory for algorithm creation, Strategy for different processing steps, Observer for state updates.
*   **Enhance Abstraction:** Introduce more interfaces for services or data access to further decouple components.
*   **Improve Encapsulation:** Review classes for any overly exposed internal state.
*   **Comprehensive Unit Testing:** Use JUnit to test individual class responsibilities and method contracts.

## 10. Author

*   Arin Bansal (OOPS Project - B1)

---
