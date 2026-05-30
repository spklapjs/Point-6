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

This project is a mini-project for the Mobile Computing and Its Applications course. It focuses on developing an intelligent, motion-based rhythm game that leverages distributed mobile sensors and parallel AI inference.

---

### 2. Goal and Usage Scenario

Project Goal

The primary objective is to develop point-6, an intelligent rhythm game application that performs motion recognition by running parallel inference on 6-axis sensor data from a smartphone and 2-axis data from an S-Pen. By analyzing inertial and relative 2D motion data through two independent Deep Neural Networks, the system determines strikes within six virtual drum zones. The project aims to achieve over 90 percent accuracy and under 50ms processing latency for a seamless real-time experience.

Usage Scenario

1. Setup: The user holds a smartphone and S-Pen in each hand.
2. Calibration: A quick initialization process sets the virtual coordinate system based on the user's resting position.
3. Play: The user strikes six virtual drum zones in the air using both hands. The parallel model architecture allows for simultaneous strikes, just like playing a real drum kit.
4. Feedback: The application provides immediate multi-channel audio, haptic, and color-based feedback based on the independently classified motions from each hand.

---

### 3. Related Works

Conventional rhythm games generally depend on dedicated hardware or require the user to maintain focus on a screen. Existing mobile rhythm games are often limited to touch-based input or single-device IMU sensing, which restricts the degrees of freedom in 3D space. While recent research has advanced mobile activity recognition, most studies focus on single-point sensing. point-6 differentiates itself by processing time-series data across two separate devices simultaneously using independent neural networks, enabling complex motion pattern tracking, simultaneous multi-hit recognition, and an eyes-free gaming experience.

---

### 4. Key Idea

The core concept is the dual-model parallel inference and the implementation of 6 virtual drum zone control. Instead of fusing data, the system utilizes two independent CNN-LSTM models for the smartphone (6-axis) and S-Pen (2-axis). This parallel architecture allows the system to recognize simultaneous multi-instrument strikes, providing a realistic drumming experience. To enhance usability in motion-heavy scenarios, the project follows a Non-Visual Dominance design, prioritizing audio and haptic feedback. For efficient on-device execution, model optimization techniques such as pruning and INT8 quantization are applied via TensorFlow Lite.

---

### 5. Project Timeline

* Week 1: Finalizing requirements and setting up the development environment.
* Week 2: Constructing the continuous stream data pipeline and collecting training datasets for 6 virtual drum motions.
* Week 3: Designing and training two independent CNN-LSTM models for the smartphone and S-Pen.
* Week 4: Applying model pruning and quantization and integrating both models into the Android parallel inference engine.
* Week 5: Conducting system integration tests, evaluating simultaneous strike latency, and preparing the final demonstration.
