# 05_Detailed_Software_Architecture_Specification

## Version History

| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2024-05-17 | Initial release |

## 1. Project Directory Structure

```text
com.spklapjs.point6
├── data
│   ├── sensor
│   │   ├── SPenManager.kt
│   │   ├── BudsManager.kt
│   │   └── PhoneSensorManager.kt
│   ├── audio
│   │   └── AssetAudioSource.kt
│   └── repository
│       └── SensorRepositoryImpl.kt
├── domain
│   ├── model
│   │   ├── SensorWindow.kt
│   │   └── DrumType.kt
│   ├── repository
│   │   └── SensorRepository.kt
│   ├── usecase
│   │   ├── DataSyncUseCase.kt
│   │   └── GetInferenceUseCase.kt
│   ├── ai
│   │   └── InferenceEngine.kt
│   └── game
│       ├── BeatManager.kt
│       ├── CalibrationManager.kt
│       └── JudgeManager.kt
└── presentation
    ├── view
    │   ├── MainActivity.kt
    │   ├── CalibrationActivity.kt
    │   ├── PlayActivity.kt
    │   └── StageSelectActivity.kt
    ├── viewmodel
    │   ├── MainViewModel.kt
    │   └── GameViewModel.kt
    └── feedback
        ├── SpatialAudioEngine.kt
        └── HapticController.kt
```


## 2. Component Descriptions and Responsibilities

### Data Layer
The data layer is responsible for direct hardware communication, low-level API handling, and raw resource management.
```text
sensor/SPenManager.kt
Integrates the Samsung S-Pen Remote SDK to capture raw button events and inertial sensor streams from the stylus.

sensor/BudsManager.kt
Interfaces with the Galaxy Buds API to retrieve real-time head tracking and coordinate data.

sensor/PhoneSensorManager.kt
Accesses the Android SensorManager to extract high-frequency IMU data from the internal smartphone accelerometer and gyroscope.

audio/AssetAudioSource.kt
Manages raw audio assets, including background music tracks and specific drum sound effects stored in the application assets.

repository/SensorRepositoryImpl.kt
Implements the unified sensor data collection interface, serving as the central hub for gathering raw streams from all three device managers.
```

### Domain Layer
The domain layer contains the pure business logic, synchronization pipeline, and the abstract rules governing the game and AI model operations.
```text
model/SensorWindow.kt
A data class designed to hold the structured, 18-axis synchronized sensor data frame ready for model input.

model/DrumType.kt
An enumeration defining the seven target instruments: Snare, Tom1, Tom2, Cymbal1, Cymbal2, Hi-hat, and Kick.

repository/SensorRepository.kt
An abstract interface defining sensor data collection contracts, allowing the domain layer to remain independent of specific hardware implementations.

usecase/DataSyncUseCase.kt
Contains the time-alignment algorithm that processes incoming heterogeneous sensor frequencies using linear interpolation and buffering techniques.

usecase/GetInferenceUseCase.kt
Acts as an intermediary coordinator that requests classification from the inference engine and passes the recognized drum motion to the gameplay logic.

ai/InferenceEngine.kt
Manages the TensorFlow Lite interpreter, memory allocation, and hardware acceleration to execute the optimized CNN-LSTM hybrid model on-device.

game/BeatManager.kt
Analyzes the timeline of the active background music based on its BPM to calculate the precise target window for incoming inputs.

game/CalibrationManager.kt
Handles the initial alignment of the virtual coordinate space, establishing sensor offsets based on the user's initial gaze direction.

game/JudgeManager.kt
Evaluates the timing difference between the inferred drum strike and the correct music beat to determine Perfect, Good, or Miss judgments.
```

### Presentation Layer
The presentation layer handles user interaction, state management, visual updates, and real-time sensory feedback execution.
```text
view/MainActivity.kt
The primary application entry point that manages global configuration and displays hardware pairing statuses.

view/CalibrationActivity.kt
The specialized user interface that guides the player through the zero-point sensor calibration and coordinate establishment process.

view/PlayActivity.kt
The main interactive gameplay screen that renders visual performance feedback and includes a toggle switch for real-time sensor graphs.

view/StageSelectActivity.kt
The navigation screen enabling users to choose music stages, preview tracks, and select difficulty levels.

viewmodel/MainViewModel.kt
Manages the UI state, background tasks, and validation rules for device connectivity and pre-game setups.

viewmodel/GameViewModel.kt
Controls the active game loop state, tracks score accumulation, processes real-time sensor windows, and triggers immediate multi-sensory feedback events.

feedback/SpatialAudioEngine.kt
A native C++ JNI wrapper using the Google Oboe library to deliver low-latency, 3D spatialized sound feedback corresponding to head movements.

feedback/HapticController.kt
Controls precise Android Vibrator attributes to output distinct tactile vibration patterns for performance indicators and errors.
```
