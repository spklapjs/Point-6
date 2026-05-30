# 03_Functional_Requirements

### Version History

| Date | Version | Remarks |
| --- | --- | --- |
| 2026-05-14 | 1.0 | Initial release |
| 2026-05-18 | 1.1 | Updated for 8-axis sensor fusion and 6 virtual drum zones |
| 2026-05-30 | 1.2 | Updated to parallel dual-model architecture and removed calibration phase |

---

### 1. System Initialization and Game Setup

1.1 Device Connection
Upon app launch, the Bluetooth connection status between the smartphone and S-Pen must be checked and established. The calibration phase has been deprecated due to the removal of the spatial coordinate anchor.

1.2 Play Mode Selection
The user must be able to select one of the following 3 play modes.
Free Play Mode: A mode where the user can freely produce 6 instrument sounds and enjoy the hitting sensation without a predefined pattern.
Practice Mode: A tutorial mode to repeatedly practice specific strike patterns appearing in the stages along with audio cues to build muscle memory.
Stage Mode: The main mode where gameplay progresses according to the selected music and the score is recorded.

1.3 Track and Difficulty Setting
The user must be able to select the song to play when entering Stage Mode.
Difficulty Selection (Easy, Normal, Hard): The tolerance time range for strike judgments must become narrower depending on the difficulty.

---

### 2. Real-time Multi-Sensor Data Collection and Synchronization

2.1 Multi-Device Sensor Streaming
High-frequency 6-axis IMU data (accelerometer and gyroscope) must be collected in real-time from the smartphone.
2-axis relative delta motion data must be collected in real-time from the S-Pen.

2.2 Data Synchronization and Preprocessing
Sensor data incoming from the 2 independent devices must be aligned based on their timestamps, and interpolation processing must be performed to compensate for sampling rate differences.
The collected time-series data must be buffered into separated 200ms sliding window formats to be inputted into the parallel inference models independently.

---

### 3. Machine Learning Based Motion Inference and Classification

3.1 Parallel Dual-Model Trajectory Classification
Synchronized 6-axis sensor data from the smartphone and 2-axis sensor data from the S-Pen must be inputted in real-time into their respective independent Android built-in TFLite optimized models.
The two independent CNN-LSTM models must analyze the spatial and temporal patterns of the user's swinging hands and classify in real-time which of the 6 virtual drums was struck.

3.2 Simultaneous Strike Recognition
The parallel architecture must allow the smartphone and S-Pen models to process inputs simultaneously without fusion, enabling the recognition of cross-hand simultaneous drumming combinations.

---

### 4. In-Game Play System

4.1 Audio Cue Advance Instruction System
Considering the play environment where it is difficult to look at the screen, a voice instruction or a specific signal sound must be output just before the measure requiring a specific pattern begins.

4.2 Strike Judgment and Combo System
The correct pattern instructed by the audio cue and the actual independent strike model classification results performed by the user must be compared based on the timestamp.
Depending on the beat timing and motion accuracy, the strike judgment must be processed into 3 levels (Perfect, Good, Miss) for each hand.

---

### 5. Multi-Sensory Feedback and Data Visualization

5.1 Ultra-Low Latency Multi-channel Audio Feedback
As soon as a strike motion is recognized, the audio sound of the corresponding drum must be output within a latency time of 50ms or less. The audio engine must support simultaneous multi-channel mixing to output sounds from both hands simultaneously without delay.

5.2 Macroscopic Visual Feedback and Tactile Feedback
So that the user can intuitively know the judgment result even while swinging the smartphone, the color of the entire smartphone screen must be rendered to flash based on the accuracy.
When an off-beat or incorrect motion is performed, strong haptic feedback must be generated on the smartphone.

5.3 Real-time Sensor Data Visualization
To prove the fulfillment of assignment requirements and system stability, real-time separated sensor data of the smartphone and S-Pen must be visualized in the form of a line graph.
To prevent obstructing the player's view, this visualization graph must be able to be turned on and off through a toggle switch on the UI.

---

### 6. Session Result Analysis Report

6.1 Comprehensive Statistics UI
After the stage play ends, a report screen displaying whether the song was cleared, final score, maximum combo count, and the number of times by judgment must be provided.
Statistical indicators that quantify and analyze the user's motion execution ability must be included.
