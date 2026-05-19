# point-6

### Documentation and Hyperlinks

* [Development Log](./DEV_LOG.md)
* [SDLC Specification](./docs/01_SDLC_Specification.md)
* [System Architecture](./docs/02_System_Architecture.md)
* [Functional Requirements](./docs/03_Functional_Requirements.md)
* [UI/UX Design](./docs/04_UI_UX_Design.md)
* [Detailed Software Architecture Specification](./docs/05_Detailed_Software_Architecture_Specification.md)
* [Data Pipeline and AI Model Specification](./docs/06_Data_Pipeline_and_AI_Model_Specification.md)

---

### 1. Project Overview

Author: Hoin Kim
Course: Mobile Computing and Its Applications (Spring 2026)

This project is a mini-project for the Mobile Computing and Its Applications course . It focuses on developing an intelligent, motion-based rhythm game that leverages distributed mobile sensors.

---

### 2. Goal and Usage Scenario

Project Goal

The primary objective is to develop point-6, an intelligent rhythm game application that performs motion recognition by fusing 8-axis sensor data from a smartphone and an S-Pen. By analyzing inertial and relative 2D motion data through a Deep Neural Network, the system determines strikes within six virtual drum zones. The project aims to achieve over 90 percent accuracy and under 50ms processing latency for a seamless real-time experience.

Usage Scenario

1. Setup: The user holds a smartphone and S-Pen in each hand.
2. Calibration: A quick initialization process sets the virtual coordinate system based on the user's resting position.
3. Play: The user strikes six virtual drum zones in the air using both hands.
4. Feedback: The application provides immediate audio, haptic, and color-based feedback based on the classified motions.

---

### 3. Related Works

Conventional rhythm games generally depend on dedicated hardware or require the user to maintain focus on a screen. Existing mobile rhythm games are often limited to touch-based input or single-device IMU sensing, which restricts the degrees of freedom in 3D space. While recent research has advanced mobile activity recognition, most studies focus on single-point sensing. point-6 differentiates itself by integrating 8-axis time-series data across two separate devices to enable complex motion pattern tracking and an eyes-free gaming experience through spatial sensor fusion.

---

### 4. Key Idea

The core concept is the 8-axis multi-point sensor fusion and the implementation of 6 virtual drum zone control. Since the S-Pen provides 2-axis delta data instead of absolute 3D coordinates, the system utilizes a CNN-LSTM hybrid model to process spatial and temporal data patterns simultaneously for pattern recognition rather than absolute coordinate tracking. To enhance usability in motion-heavy scenarios, the project follows a Non-Visual Dominance design, prioritizing audio and haptic feedback. For efficient on-device execution, model optimization techniques such as pruning and INT8 quantization are applied via TensorFlow Lite.

---

### 5. Project Timeline

* Week 1: Finalizing requirements and setting up the development environment.
* Week 2: Constructing the data pipeline and collecting training datasets for 6 virtual drum motions.
* Week 3: Designing and training the CNN-LSTM hybrid model.
* Week 4: Applying model pruning and quantization and integrating with the Android application.
* Week 5: Conducting system integration tests and preparing the final demonstration.
