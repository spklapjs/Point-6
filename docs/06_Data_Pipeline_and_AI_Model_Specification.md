# 06_Data_Pipeline_and_AI_Model_Specification

### Version History

| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2026-05-19 | Initial release |
| 1.1 | 2026-05-30 | Updated for parallel independent streams and models |

### 1. Data Collection Phase

- Tool: Logger activity application in the presentation layer on the Android device.
- Method: The user holds a smartphone and an S-Pen in each hand and performs striking motions towards 6 virtual drum zones (Snare, Tom1, Tom2, Cymbal1, Cymbal2, Hi-hat).
- Format: Recorded as separate continuous CSV files for the smartphone and S-Pen in the local storage of the Android device.
- Structure: The smartphone CSV file logs continuous sequential data streams containing timestamps, 3-axis accelerometer, 3-axis gyroscope, and the target label. The S-Pen CSV file independently logs timestamps, 2-axis delta values, and the target label. Overlapping window segmentation is no longer performed during collection.

### 2. Data Transfer Phase

- Transfer: The raw CSV files generated in the local storage are moved to a PC and uploaded to the designated raw data folder in Google Drive.
- Environment: Execute Google Colab environment and mount Google Drive storage.

### 3. Data Preprocessing Phase

- Environment: 01_data_preprocessing.ipynb script in Google Colab Python environment.
- Process:
  1. Load Data: Read continuous CSV files from the raw folder into Pandas dataframes independently for the smartphone and S-Pen.
  2. Peak Detection and Centering: Scan the continuous streams to detect valid strike peaks using amplitude threshold constraints. For the smartphone, peak detection is based on the z-axis acceleration. For the S-Pen, it is based on the y-axis delta motion. Extract a 200ms window exactly centered around the detected maximum peak for each device.
  3. Normalization: Apply standard scaling (mean 0, variance 1) to the 6-axis and 2-axis tensors independently. Export separate scaling parameters to json format to be used in Android real-time inference.
  4. Save Data: Save the processed tensors as separate independent training datasets for the smartphone and S-Pen in the processed folder.

### 4. AI Model Functionality and Specifications

- Model Function: The system runs two independent classification engines to recognize virtual drum strikes simultaneously.
- Architecture: Two independent CNN-LSTM hybrid neural networks built using the PyTorch framework.
- Input Specification: The smartphone model receives a normalized 6-axis 200ms window tensor. The S-Pen model receives a normalized 2-axis 200ms window tensor.
- Output Specification: Each model independently outputs a 1-dimensional probability array of size 6 representing the confidence scores for the 6 virtual drum classes.

### 5. Model Optimization and Conversion Phase

- Environment: 03_model_optimization.ipynb script in Google Colab.
- Pruning Technique: Channel Pruning. The target sparsity must be set to 93 percent or higher.
- Quantization Technique: INT8 Linear Quantization to accelerate inference speed on mobile hardware.
- Performance Constraints: Classification accuracy must remain higher than 86 percent. Independent inference latency on the Android device must not exceed 50ms.
- Format Conversion Pipeline: Export to .pth, convert to .onnx, and finally to .tflite. Two separate TFLite files (smartphone_model.tflite, spen_model.tflite) are placed in the Android assets directory.

### 6. Real-time System Integration and Communication Flow

- Continuous Buffering: During gameplay, DataSyncUseCase acts as a simple buffer that groups continuous streams into 200ms windows independently for each device, without complex timestamp fusion.
- Parallel Inference Request: GetInferenceUseCase sends parallel asynchronous requests to both SmartphoneInferenceEngine and SPenInferenceEngine.
- Execution and Return: Each engine normalizes the input using the saved JSON parameters, runs its specific TFLite model, and independently returns its predicted drum class.
- Game Logic Linkage: GameViewModel receives the independent results, allowing JudgeManager to evaluate simultaneous strikes and trigger immediate multi-sensory feedback.
