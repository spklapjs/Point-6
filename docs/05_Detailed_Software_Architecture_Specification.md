# 05_Detailed_Software_Architecture_Specification

### Version History

| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2026-05-17 | Initial release |
| 1.1 | 2026-05-17 | Added Logger package and ai_model pipeline structure |
| 1.2 | 2026-05-18 | Updated for 8-axis system, removed BudsManager and spatial audio |
| 1.3 | 2026-05-29 | Deleted ai_model/src directory |
| 1.4 | 2026-05-30 | Updated for parallel dual-model architecture and removed calibration phase |

### 1. Project Directory Structure

```text
point-6
├── ai_model
│   ├── data
│   │   ├── raw
│   │   └── processed
│   ├── notebooks
│   │   ├── 01_data_preprocessing.ipynb
│   │   ├── 02_model_training.ipynb
│   │   └── 03_model_optimization.ipynb
│   ├── checkpoints
│   └── exported_models
│       ├── smartphone_model.tflite
│       ├── spen_model.tflite
│       ├── smartphone_scaling.json
│       └── spen_scaling.json
└── app
    └── src/main/java/com/spklapjs/point_6
        ├── data
        │   ├── sensor
        │   │   ├── SPenManager.kt
        │   │   └── PhoneSensorManager.kt
        │   ├── inference
        │   │   ├── SmartphoneInferenceEngine.kt
        │   │   └── SPenInferenceEngine.kt
        │   ├── audio
        │   │   └── AssetAudioSource.kt
        │   └── repository
        │       └── SensorRepositoryImpl.kt
        ├── domain
        │   ├── model
        │   │   ├── PhoneSensorWindow.kt
        │   │   ├── SPenSensorWindow.kt
        │   │   └── DrumType.kt
        │   ├── repository
        │   │   └── SensorRepository.kt
        │   ├── usecase
        │   │   ├── DataSyncUseCase.kt
        │   │   └── GetInferenceUseCase.kt
        │   ├── game
        │   │   ├── BeatManager.kt
        │   │   └── JudgeManager.kt
        └── presentation
            ├── view
            │   ├── MainActivity.kt
            │   ├── PlayActivity.kt
            │   ├── StageSelectActivity.kt
            │   └── LoggerActivity.kt
            ├── viewmodel
            │   ├── MainViewModel.kt
            │   ├── GameViewModel.kt
            │   └── LoggerViewModel.kt
            └── feedback
                ├── AudioEngine.kt
                └── HapticController.kt
```

### 2. Component Descriptions and Responsibilities
#### Data Layer
- The data layer is responsible for direct hardware communication, low-level API handling, and raw resource management.
```text 
sensor/SPenManager.kt
Integrates the Samsung S-Pen Remote SDK to capture raw button events and 2-axis relative delta motion streams from the stylus.

sensor/PhoneSensorManager.kt
Accesses the Android SensorManager to extract high-frequency 6-axis IMU data from the internal smartphone linear accelerometer and gyroscope.

inference/SmartphoneInferenceEngine.kt
Manages the TensorFlow Lite interpreter to execute the optimized 6-axis CNN-LSTM model for smartphone data. Applies real-time scaling using parameters loaded from smartphone_scaling.json.

inference/SPenInferenceEngine.kt
Manages the TensorFlow Lite interpreter to execute the optimized 2-axis CNN-LSTM model for S-Pen data. Applies real-time scaling using parameters loaded from spen_scaling.json.

audio/AssetAudioSource.kt
Manages raw audio assets, including background music tracks and specific drum sound effects stored in the application assets.

repository/SensorRepositoryImpl.kt
Implements the sensor data collection interface, serving as the central hub for gathering raw streams from the two independent device managers.
```
#### Domain Layer
- The domain layer contains the pure business logic, synchronization pipeline, and the abstract rules governing the game and AI model operations.
```text
model/PhoneSensorWindow.kt
A data class designed to hold the structured, 6-axis smartphone sensor data frame ready for independent model input.

model/SPenSensorWindow.kt
A data class designed to hold the structured, 2-axis S-Pen sensor data frame ready for independent model input.

model/DrumType.kt
An enumeration defining the 6 target virtual instruments: Snare, Tom1, Tom2, Cymbal1, Cymbal2, and Hi-hat.

repository/SensorRepository.kt
An abstract interface defining sensor data collection contracts, allowing the domain layer to remain independent of specific hardware implementations.

usecase/DataSyncUseCase.kt
Contains the time-alignment algorithm that processes incoming heterogeneous sensor frequencies to create continuous, separated 200ms windows for the smartphone and S-Pen.

usecase/GetInferenceUseCase.kt
Acts as a coordinator that requests parallel classification from the dual inference engines and passes a list of recognized independent drum motions to the gameplay logic.

game/BeatManager.kt
Analyzes the timeline of the active background music based on its BPM to calculate the precise target window for incoming inputs.

game/JudgeManager.kt
Evaluates the timing difference between the inferred drum strikes from both hands and the correct music beat to determine Perfect, Good, or Miss judgments. Supports simultaneous strike pattern evaluation.
```
#### Presentation Layer
- The presentation layer handles user interaction, state management, visual updates, and real-time sensory feedback execution.
```text
view/MainActivity.kt
The primary application entry point that manages global configuration and displays hardware pairing statuses.

view/PlayActivity.kt
The main interactive gameplay screen that renders visual performance feedback and includes a toggle switch for real-time separated sensor graphs.

view/StageSelectActivity.kt
The navigation screen enabling users to choose music stages, preview tracks, and select difficulty levels.

view/LoggerActivity.kt
Provides a dedicated developer dashboard to facilitate continuous stream data acquisition. Includes dropdown menus to label targets and start/stop recording.

viewmodel/MainViewModel.kt
Manages the UI state, background tasks, and validation rules for device connectivity.

viewmodel/GameViewModel.kt
Controls the active game loop state, tracks score accumulation, processes parallel AI predictions, and triggers immediate multi-sensory feedback events.

viewmodel/LoggerViewModel.kt
Interacts with SensorRepository to fetch streaming data and handles local file output processes to export continuous CSV data blocks without window slicing.

feedback/AudioEngine.kt
A native C++ JNI wrapper using the Google Oboe library to deliver low-latency multi-channel audio mixing, allowing multiple drum sounds to play simultaneously without delay.

feedback/HapticController.kt
Controls precise Android Vibrator attributes to output distinct tactile vibration patterns for performance indicators and errors.
```
#### AI Model Pipeline Layer
- The ai_model folder encapsulates the complete deep learning lifecycle from data preparation to optimization for the dual models.
```text
data/raw/
Contains continuous streaming original raw signals generated by LoggerActivity in comma-separated values format.

data/processed/
Stores separated 6-axis and 2-axis time-series data datasets that have been centrally aligned and normalized, ready for independent network training.

notebooks/01_data_preprocessing.ipynb
Execution script for continuous data parsing, peak alignment at the center of the 200ms window, and feature scaling. Exports scaling_params json files.

notebooks/02_model_training.ipynb
Configures two separate CNN-LSTM neural network architectures using PyTorch and applies dynamic Gaussian noise for robust training.

notebooks/03_model_optimization.ipynb
Implements channel pruning matrices and INT8 linear quantization operations, exporting dual compressed TFLite models.
```
