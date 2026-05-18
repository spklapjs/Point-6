# point-6 Functional Requirements Specification

### Version History

| Date | Version | Remarks |
| :--- | :--- | :--- |
| 2026-05-14 | 1.0 | Initial release |
| 2026-05-18 | 1.1 | Updated for 8-axis sensor fusion (Smartphone and S-Pen) and 6 virtual drum zones |

---

### 1. System Initialization and Game Setup

1.1 Device Synchronization and Calibration
- Upon app launch, the Bluetooth connection status between the smartphone and S-Pen must be checked.
- Before starting the game, a calibration feature must be provided to set the default standby state and sensor zero points for both hands (smartphone and S-Pen) when the user is standing in a neutral posture.

1.2 Play Mode Selection (Game Modes)
- The user must be able to select one of the following 3 play modes.
- Free Play Mode (Free Play): A mode where the user can freely produce 6 instrument sounds and enjoy the hitting sensation without a predefined pattern. In this mode, turning the background music on or off is possible.
- Practice Mode (Practice Mode): A tutorial mode to repeatedly practice specific strike patterns (e.g., Pattern 1=1-6-3-6) appearing in the stages along with audio cues to build muscle memory.
- Stage Mode (Stage Mode): The main mode where gameplay progresses according to the selected music and the score is recorded.

1.3 Track and Difficulty Setting (Track & Difficulty)
- The user must be able to select the song (BGM) to play when entering Stage Mode.
- Difficulty Selection (Easy, Normal, Hard): The tolerance time range for strike judgments must become narrower depending on the difficulty.

---

### 2. Real-time Multi-Sensor Data Collection and Synchronization

2.1 Multi-Device Sensor Streaming
- High-frequency 6-axis IMU data (accelerometer and gyroscope) must be collected in real-time from the smartphone.
- 2-axis relative delta motion data must be collected in real-time from the S-Pen.

2.2 Data Synchronization and Preprocessing
- Sensor data incoming from 2 independent devices must be aligned based on their timestamps, and interpolation processing must be performed to compensate for sampling rate differences for synchronization.
- The collected time-series data must be buffered in a sliding window format to be input into the inference model.

---

### 3. Machine Learning Based Motion Inference and Classification (HAR)

3.1 Both Hands Swing Trajectory Classification (CNN-LSTM)
- Synchronized 8-axis sensor data from the smartphone and S-Pen must be input in real-time into the Android built-in TFLite optimized model (CNN-LSTM).
- The model must analyze the acceleration/gyro patterns and 2D relative motion patterns of the user's swinging hands and classify in real-time which of the 6 virtual drums (snare, tom-tom, cymbals, etc.) was struck.

---

### 4. In-Game Play System (Audio Cue Based)

4.1 Audio Cue Advance Instruction System
- Considering the play environment where it is difficult to look at the screen, a voice instruction like "Pattern 3!" or a specific signal sound (audio cue) must be output just before the measure requiring a specific pattern begins, allowing the user to recognize the motion to take in the next measure in advance.

4.2 Strike Judgment and Combo System
- The correct pattern instructed by the audio cue and the actual strike model classification result performed by the user must be compared based on the timestamp.
- Depending on the beat timing and motion accuracy, the strike judgment must be processed into 3 levels (Perfect, Good, Miss).

---

### 5. Multi-Sensory Feedback and Data Visualization

5.1 Ultra-Low Latency Audio Feedback
- As soon as a strike motion is recognized, the stereo audio sound of the corresponding drum must be output within a latency time of 50ms or less (Utilizing AAudio/Oboe library).

5.2 Macroscopic Visual Feedback and Tactile Feedback
- So that the user can intuitively know the judgment result even while swinging the smartphone, the color of the entire smartphone screen must be rendered to flash (Perfect=Green, Good=Orange, Miss=Red, etc.) at the moment of the strike.
- When an off-beat or incorrect motion (Miss judgment) is performed, strong haptic (vibration) feedback must be generated on the smartphone so that the user can recognize the incorrect answer without breaking immersion.

5.3 [For Evaluation/Developer] Real-time Sensor Data Visualization (Toggle)
- To prove the fulfillment of assignment requirements and system stability, real-time sensor data of the smartphone and S-Pen must be visualized and shown in the form of a line graph on one side of the screen.
- To prevent obstructing the player's view and wasting rendering resources, this visualization graph must be able to be turned on and off through a toggle switch on the UI.

---

### 6. Session Result Analysis Report

6.1 Comprehensive Statistics UI
- After the stage play ends, a report screen displaying whether the song was cleared, final score, maximum combo count, and the number of times by judgment (Perfect/Good/Miss ratio) must be provided.
- Statistical indicators that quantify and analyze the user's motion execution ability, such as the model's motion recognition accuracy (e.g., "Accuracy at Pattern 2 is 92%"), must be included.
