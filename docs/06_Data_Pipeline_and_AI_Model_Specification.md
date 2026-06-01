# 06_Data_Pipeline_and_AI_Model_Specification

### Version History

| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2026-05-19 | Initial release |
| 1.1 | 2026-05-30 | Updated for parallel independent streams and models |
| 1.2 | 2026-06-01 | Updated optimization phase to local script execution and dynamic range quantization due to framework limitations |

### 1. Data Collection Phase
- Tool: Logger activity application in the presentation layer on the Android device.
- Method: The user holds a smartphone and an S-Pen in each hand and performs striking motions towards 6 virtual drum zones (Snare, Tom1, Tom2, Cymbal1, Cymbal2, Hi-hat).
- Format: Recorded as separate continuous CSV files for the smartphone and S-Pen in the local storage of the Android device.
- Structure: The smartphone CSV file logs continuous sequential data streams containing timestamps, 3-axis accelerometer, 3-axis gyroscope, and the target label. The S-Pen CSV file independently logs timestamps, 2-axis delta values, and the target label.

### 2. Data Transfer Phase
- Transfer: The raw CSV files generated in the local storage are moved to a PC and uploaded to the designated raw data folder in Google Drive.
- Environment: Execute Google Colab environment and mount Google Drive storage.

### 3. Data Preprocessing Phase
- Environment: 01_data_preprocessing.ipynb script in Google Colab Python environment.
- Process:
1. Load Data: Read continuous CSV files from the raw folder into Pandas dataframes independently for the smartphone and S-Pen.
2. Peak Detection and Centering: Scan the continuous streams to detect valid strike peaks using amplitude threshold constraints. Extract a 200ms window exactly centered around the detected maximum peak for each device.
3. Normalization: Apply standard scaling (mean 0, variance 1) to the 6-axis and 2-axis tensors independently. Export separate scaling parameters to json format.
4. Save Data: Save the processed tensors as separate independent training datasets.

### 4. AI Model Functionality and Specifications
- Model Function: The system runs two independent classification engines to recognize virtual drum strikes simultaneously.
- Architecture: Two independent CNN-LSTM hybrid neural networks built using the PyTorch framework.
- Input Specification: The smartphone model receives a normalized 6-axis 200ms window tensor. The S-Pen model receives a normalized 2-axis 200ms window tensor.
- Output Specification: Each model independently outputs a 1-dimensional probability array of size 6 representing the confidence scores for the 6 virtual drum classes.

### 5. Model Optimization and Conversion Phase
- Environment: Local Python script (optimize_and_convert.py) executed in an isolated Conda virtual environment to prevent severe dependency collisions (e.g., protobuf, jax, ml_dtypes) inherent in cloud notebook environments.
- Process:
1. Channel Pruning: Apply structured channel pruning to the trained models targeting a 93 percent sparsity rate to reduce computational parameters without distorting spatial feature extraction.
2. Fine-tuning: Execute a 50-epoch retraining loop on the pruned models to recover lost accuracy, tracking dynamic validation accuracy locally.
3. ONNX Extraction: Export the recovered models to ONNX representations. The onnxsim step is deliberately bypassed to prevent the distortion of LSTM initial state dimensions.
4. TFLite Conversion & Dynamic Range Quantization: Convert the intermediate ONNX graphs to TFLite format using onnx2tf via a subprocess call. The calibration tensor arrays are transposed from PyTorch's channel-first to TensorFlow's channel-last dimension format. Full Integer Quantization is replaced with Dynamic Range Quantization because the C++ quantization runtime causes memory access violations (0xC0000005) when mapping the recurrent loops of the CNN-LSTM hybrid structure. This hybrid quantization approach compresses the weights to INT8 while keeping activations dynamic, successfully resolving framework constraints while ensuring hardware acceleration.

### 6. Real-time System Integration and Communication Flow
- Continuous Buffering: During gameplay, DataSyncUseCase acts as a simple buffer that groups continuous streams into 200ms windows independently for each device.
- Parallel Inference Request: GetInferenceUseCase sends parallel asynchronous requests to both SmartphoneInferenceEngine and SPenInferenceEngine.
- Execution and Return: Each engine normalizes the input using the saved JSON parameters, runs its specific TFLite model, and independently returns its predicted drum class.
- Game Logic Linkage: GameViewModel receives the independent results, allowing JudgeManager to evaluate simultaneous strikes and trigger immediate multi-sensory feedback.
