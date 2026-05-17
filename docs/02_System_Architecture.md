# point6 System Architecture Specification

### Version History

| Date | Version | Remarks |
| :--- | :--- | :--- |
| 2026-05-14 | 1.0 | Initial release |
| 2026-05-18 | 1.1 | Updated pipeline and MVVM plus AI architecture based on detailed specification |

---

### 1. Technology Stack

1.1. Frontend and UI Environment

Language: Kotlin (Android Native)

IDE: Android Studio

Pattern: MVVM (Model-View-ViewModel)

1.2. Machine Learning and Optimization

Training: Python, PyTorch (Google Colab)

Optimization: PyTorch Pruning API (Channel Pruning)

Inference: TensorFlow Lite (INT8 Linear Quantization)

1.3. Hardware Sensor Communication

Smartphone: Android SensorManager API (Linear Acceleration, Gyroscope)

S-Pen: Samsung S-Pen Remote SDK (BLE)

Galaxy Buds: Android Bluetooth and Sensor API (Head Tracking)

1.4. Media and Feedback Processing

Audio Engine: Google Oboe

Haptic: Android Vibrator API

---

### 2. Five-Layer Data Pipeline

The system processes data through a structured 5-layer pipeline to ensure real-time performance and accuracy.

```mermaid
graph TD
    subgraph L1 [Layer 1: Data Collection]
        A[Smartphone IMU] --> D[SensorRepositoryImpl]
        B[S-Pen IMU] --> D
        C[Buds IMU] --> D
    end

    subgraph L2 [Layer 2: Sync and Preprocess]
        D -- Raw Data --> E[DataSyncUseCase]
        E -- Timestamp Alignment & Sliding Window --> F[Windowed Data]
    end

    subgraph L3 [Layer 3: AI Inference]
        F -- Request --> G[GetInferenceUseCase]
        G -- Call --> H[InferenceEngineImpl]
        H -- CNN-LSTM classification --> I[Predicted Class]
    end

    subgraph L4 [Layer 4: Game Business Logic]
        I --> J[BeatManager & JudgeManager]
        J -- Compare audio cue & inference --> K[Game State]
    end

    subgraph L5 [Layer 5: Multi-sensory Feedback]
        K --> L[Presentation UI]
        K --> M[AudioEngine]
    end
```

Layer 1 (Data Collection): The data layer utilizes SpenManager, BudsManager, and SensorRepositoryImpl to stream 18-axis raw data from three heterogeneous devices.

Layer 2 (Sync and Preprocess): The domain layer utilizes DataSyncUseCase to align timestamps and create 200ms overlapping windows from the multi-frequency sensor data.

Layer 3 (AI Inference): The domain layer uses GetInferenceUseCase to call InferenceEngineImpl in the data layer, which classifies the 7 instrument motions in real-time.

Layer 4 (Game Business Logic): The domain layer manages the game rules through BeatManager and JudgeManager, comparing audio cue beats with the user strike inference results.

Layer 5 (Multi-sensory Feedback): The presentation layer and AudioEngine deliver immediate multi-sensory responses, including screen color changes, haptic vibrations, and 3D spatial audio.

---

### 3. Software Architecture (MVVM plus AI)

The application follows an extended 4-layer architecture separating the offline AI pipeline from the runtime MVVM structure.

```mermaid
graph TD
    subgraph AI [AI Model Pipeline Layer]
        P1[Python Environment] -- Preprocess & Train --> P2[CNN-LSTM Model]
        P2 -- Pruning & Quantization --> P3[TFLite Model]
    end

    subgraph Data [Data Layer]
        D1[Hardware Managers] --> D2[SensorRepositoryImpl]
        D3[InferenceEngineImpl]
    end
    
    P3 -. Loads .-> D3

    subgraph Domain [Domain Layer]
        U1[DataSyncUseCase]
        U2[GetInferenceUseCase]
        U3[BeatManager & JudgeManager]
    end

    subgraph Presentation [Presentation Layer]
        VM[GameViewModel]
        V1[PlayActivity]
    end

    D2 -- Raw Data --> U1
    U1 -- Synchronized Windows --> U2
    U2 -- Request Inference --> D3
    D3 -- Predicted Class --> U2
    U2 -- Result --> VM
    VM <-- Manage Rules --> U3
    VM -- State Updates --> V1
    V1 -- User Action / Observes --> VM
```

3.1. AI Model Pipeline Layer
An offline prerequisite layer operating in a Python environment. It handles raw data preprocessing, CNN-LSTM hybrid model training, channel pruning, and INT8 linear quantization to export the final TensorFlow Lite model.

3.2. Data Layer (Model)
Responsible for direct hardware communication and low-level API handling. It contains the SensorRepositoryImpl, hardware managers, and the InferenceEngineImpl which loads the TFLite model. This layer acts as the Model in MVVM, providing refined data and AI predictions to the ViewModel.

3.3. Domain Layer
Contains the pure business logic and synchronization pipeline. It houses DataSyncUseCase, GetInferenceUseCase, BeatManager, and JudgeManager, serving as the bridge between raw data processing and UI state generation.

3.4. Presentation Layer (View and ViewModel)
Handles user interaction, state management, and real-time sensory feedback execution. Divided into main, game, and logger packages. The GameViewModel communicates with the Model (Data and Domain layers) to request AI predictions and updates the PlayActivity view based on the returned instrument classes and game states.
