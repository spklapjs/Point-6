# point6 SDLC Specification

Author: Hoin Kim

Project: point6 (Multi-Sensor Fusion Intelligent Rhythm Game)

### Version History

| Date | Version | Remarks |
| :--- | :--- | :--- |
| 2026-05-14 | 1.0 | Initial release |

---

### Phase 0: Requirements Analysis and Specification

The objective of this phase is to define the target metrics and essential functionalities of the system before commencing development.

Functional Requirements:
1. Multi-device Data Acquisition: Real-time collection and synchronization of IMU and head-tracking data from a smartphone, S-Pen, and Galaxy Buds.
2. Dynamic Coordinate System: Utilizing Galaxy Buds data as a reference anchor to dynamically adjust the 6 virtual drum zones based on the user's line of sight.
3. Real-time Inference: Processing time-series trajectory data through a CNN-LSTM hybrid model to classify drum strikes in real-time.
4. Feedback System: Rendering 3D trajectories and providing immediate audio and color-based feedback upon strike recognition.

Non-functional Requirements:
1. Low Latency: Total system latency from strike motion to audio output must remain below 50ms.
2. High Accuracy: The motion classification model must achieve over 90 percent accuracy across 7 different instrument motions.
3. Resource Efficiency: The AI model must be optimized via pruning and quantization to ensure stable execution on mobile hardware.

---

### Phase 1: System Design and Architecture

This phase focuses on defining the technical stack and designing the software structure to satisfy the requirements.

1. Data Pipeline Design: Establishing a 5-layer architecture encompassing sensor data acquisition, synchronization, DNN inference, game logic, and feedback.
2. Software Architecture: Implementing the MVVM (Model-View-ViewModel) pattern to separate hardware control from game logic and UI.
3. UI/UX Planning: Designing the calibration sequence, play interface with non-visual dominance, and the statistical result report screen.
4. Technical Stack: Android Studio (Kotlin), S-Pen Remote SDK, Android Sensor API, and Google Oboe/AAudio for low-latency audio.

---

### Phase 2: Data Pipeline Construction and Collection

Implementation of the infrastructure for real-time sensing and building the training dataset.

1. Integrated Sensing Module: Developing the Android module to read high-frequency inertial data and head-tracking data from three heterogeneous devices.
2. Data Preprocessing: Implementing logic for timestamp synchronization across different sampling frequencies and applying linear interpolation for missing values.
3. Training Dataset Construction: Capturing and labeling motion data by performing strikes on 6 virtual drum zones and head-nodding gestures for the kick drum.

---

### Phase 3: AI Model Development and Training

Design and training of the deep learning model specialized for multi-sensor fusion.

1. Hybrid Model Design: Implementing a CNN-LSTM hybrid architecture using PyTorch to extract both spatial features and temporal sequences simultaneously.
2. Offline Training: Iterative training and hyperparameter tuning using the collected dataset until the 90 percent accuracy threshold is reached.

---

### Phase 4: Model Optimization and Mobile Porting

Optimizing the heavy DNN model for high-performance execution in the mobile environment.

1. Optimization Techniques: Applying channel pruning and INT8 linear quantization to reduce model size and inference latency.
2. TFLite Integration: Converting the trained model to TensorFlow Lite format and integrating it into the Android inference engine.

---

### Phase 5: System Integration and Final Testing

Combining all modules and validating the system against performance targets.

1. Integration Testing: Validating the end-to-end data flow from sensor input to audio-haptic feedback.
2. Latency and Accuracy Validation: Measuring processing time and classification precision in various environments to ensure the 50ms and 90 percent accuracy targets.

---

### Phase 6: Final Presentation and Release

1. Final Demo Preparation: Creating recorded gameplay footage and performance comparison reports (Before vs. After Optimization).
2. Presentation: Demonstrating the technical innovation of distributed mobile sensing and 18-axis sensor fusion.
