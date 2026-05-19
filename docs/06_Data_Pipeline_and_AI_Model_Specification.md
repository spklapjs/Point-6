# 06 Data Pipeline and AI Model Specification

## Version History
| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2026-05-19 | Initial release |

## 1. Data Collection Phase
- Tool: Logger activity application in the presentation layer on the Android device.
- Method: The user holds a smartphone and an S-Pen in each hand and performs striking motions towards 6 virtual drum zones (Snare, Tom1, Tom2, Cymbal1, Cymbal2, Hi-hat). To ensure efficiency, the user continuously strikes 20 to 30 times per label after starting the collection.
- Format: Recorded as CSV files in the local storage of the Android device.
- Structure: The CSV file consists of startTime, endTime, phoneAccel, phoneGyro, spenDelta, and label columns. The 6-axis data from the smartphone and 2-axis data from the S-Pen are synchronized based on timestamps and grouped into 200ms overlapped sliding window frames. Each cell contains an array of sensor frame numbers separated by semicolons.

## 2. Data Transfer Phase
- Transfer: The raw CSV files generated in the local storage of the Android device are manually moved to a PC and uploaded to the designated raw data folder (ai_model/data/raw) in Google Drive.
- Environment: Execute Google Colab environment and mount Google Drive storage to link the path so the scripts can directly access the uploaded raw data files.

## 3. Data Preprocessing Phase
- Environment: 01_data_preprocessing.ipynb script in Google Colab Python environment.
- Process:
  1. Load Data: Read CSV files from the mounted raw folder into Pandas dataframes.
  2. Peak Detection and Filtering: Iterate through the 200ms sensor array data continuously recorded in a single cell. Apply a specific amplitude threshold constraint. If the combined vector amplitude exceeds this threshold, it is recognized as a valid strike.
  3. Multi-Peak Handling Rule: If multiple peaks are detected within a single 200ms window, they are treated as physical noise or post-strike rebound. The rule-based script will filter these out by selecting only the single peak with the maximum amplitude value as the actual strike moment.
  4. Feature Matrix Parsing: Parse and split the extracted valid window frames into time-series feature tensor matrices that fit the input specifications of the AI model.
  5. Save Data: The processed tensor-format training datasets are saved in the processed folder.

## 4. AI Model Functionality and Specifications
- Model Function: The model acts as the core inference classification engine of the rhythm game. It processes spatial and temporal patterns from the synchronized 8-axis sensor data to recognize which virtual drum the user intended to strike in 3D space.
- Architecture: CNN-LSTM hybrid neural network using the PyTorch framework.
- Input Specification: In the live application and training phase, the model receives a continuous stream of time-series data. The input is a 200ms sliding window frame containing synchronized 8-axis values (3-axis smartphone accelerometer, 3-axis smartphone gyroscope, 2-axis S-Pen delta). It is structured as a multi-dimensional tensor matrix.
- Output Specification: The model outputs a 1-dimensional probability array of size 6. Each element in the array represents the confidence score (probability) for one of the 6 virtual drum classes (Snare, Tom1, Tom2, Cymbal1, Cymbal2, Hi-hat). The system selects the class with the highest probability as the final predicted motion.

## 5. Model Optimization and Conversion Phase
- Environment: 03_model_optimization.ipynb script in Google Colab.
- Pruning Technique: Channel Pruning. This method evaluates the importance of convolutional channels and zeroes out or completely removes less significant channels and network connection weights to reduce the physical model size.
- Pruning Constraints: The size of the pruned sparse model (number of non-zero parameters) must be lower than 7 percent of the original dense model, meaning the target sparsity must be set to 93 percent or higher.
- Quantization Technique: INT8 Linear Quantization. This method maps the original 32-bit floating-point weights and activations to 8-bit integers, drastically reducing the memory footprint and accelerating inference speed on mobile hardware.
- Performance Constraints: After applying pruning and fine-tuning, the classification accuracy for the 6 virtual drum motions must remain higher than 86 percent, aiming for the ultimate project goal of over 90 percent. The final inference latency on the Android device must not exceed 50ms.
- Format Conversion Pipeline:
  1. .pth: The optimized lightweight PyTorch model format saved after pruning and quantization.
  2. .onnx: The model is exported to the Open Neural Network Exchange (.onnx) format using torch.onnx.export() for cross-platform compatibility.
  3. .tflite: The .onnx model is finally converted to the mobile-specific TensorFlow Lite format (.tflite) using the onnx2tf library or TFLite converter.
  4. Porting: The final .tflite file is downloaded and placed in the app/src/main/assets directory of the Android Studio project, where the InferenceEngineImpl in the Android data layer loads it to perform real-time inference.

## 6. Real-time System Integration and Communication Flow
- Continuous Synchronization: During gameplay, the DataSyncUseCase in the domain layer continuously aligns the incoming heterogeneous sensor streams into 200ms overlapped sliding window frames.
- Inference Request: The GetInferenceUseCase receives the synchronized window frame and acts as a coordinator, sending an inference request to the InferenceEngineImpl located in the data layer.
- Execution and Return: The InferenceEngineImpl inputs the 200ms tensor into the loaded TFLite model, calculates the 1-dimensional probability array, extracts the virtual drum class with the highest confidence score, and returns this predicted class back to the GetInferenceUseCase.
- Game Logic Linkage: The recognized motion is then passed to the GameViewModel, which coordinates with the BeatManager and JudgeManager. The JudgeManager compares the timestamp of the inferred strike with the target window generated by the BeatManager to determine the final game state (Perfect, Good, Miss) and trigger immediate multi-sensory feedback.
