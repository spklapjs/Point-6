# Functional Requirements Specification

### Version History

| Date | Version | Remarks |
| :--- | :--- | :--- |
| 2026-05-14 | 1.0 | Initial release |

---

### 1. System Initialization and Game Setup

1.1 Device Synchronization and Calibration
* Upon app launch, the Bluetooth connection status between the smartphone, S-Pen, and Galaxy Buds must be checked.
* Before starting the game, a calibration feature must be provided to set the default standby state and sensor zero points for both hands (smartphone, S-Pen) and the head (Buds) when the user is standing in a neutral posture.

1.2 Play Mode Selection (Game Modes)
* The user must be able to select one of the following 3 play modes.
    * Free Play Mode (Free Play): A mode where the user can freely produce 7 instrument sounds and enjoy the hitting sensation without a predefined pattern. In this mode, turning the background music on or off is possible.
    * Practice Mode (Practice Mode): A tutorial mode to repeatedly practice specific strike patterns (e.g., Pattern 1=1-7-3-7) appearing in the stages along with audio cues to build muscle memory.
    * Stage Mode (Stage Mode): The main mode where gameplay progresses according to the selected music and the score is recorded.

1.3 Track and Difficulty Setting (Track & Difficulty)
* The user must be able to select the song (BGM) to play when entering Stage Mode.
* Difficulty Selection (Easy, Normal, Hard): The tolerance time range (Tolerance range) for strike judgments must become narrower depending on the difficulty.

---

### 2. Real-time Multi-Sensor Data Collection and Synchronization

2.1 Multi-Device Sensor Streaming
* High-frequency accelerometer and gyroscope (IMU) data must be collected in real-time from the smartphone and S-Pen.
* The user's head movement (IMU) and head tracking data must be collected in real-time from the Galaxy Buds.

2.2 Data Synchronization and Preprocessing
* Sensor data incoming from 3 independent devices must be aligned based on their timestamps, and interpolation (Interpolation) processing must be performed to compensate for sampling rate differences for synchronization.
* The collected time-series data must be buffered in a sliding window (Sliding Window) format to be input into the inference model.

---

### 3. Machine Learning Based Motion Inference and Classification (HAR)

3.1 Both Hands Swing Trajectory Classification (CNN-LSTM)
* Synchronized sensor data from the smartphone and S-Pen must be input in real-time into the Android built-in TFLite optimized model (CNN-LSTM).
* The model must analyze the acceleration/gyro patterns of the user's swinging hands and classify (Classification) in real-time which of the 6 virtual drums (snare, tom-tom, cymbals, etc.) was struck.

3.2 Head Gesture Recognition (Bass Drum)
* By analyzing the sensor data of the Galaxy Buds, the user's strong head 'Nodding' gesture in time with the beat must be recognized.
* This gesture must be independently classified as the strike of the 3rd instrument, 'Bass Drum (Kick Pedal)'. (Total 7 instrument sounds controlled)

---

### 4. In-Game Play System (Audio Cue Based)

4.1 Audio Cue Advance Instruction System
* Considering the play environment where it is difficult to look at the screen, a voice instruction like "Pattern 3!" or a specific signal sound (audio cue) must be output just before the measure requiring a specific pattern begins, allowing the user to recognize the motion to take in the next measure in advance.

4.2 Strike Judgment and Combo System
* The correct pattern instructed by the audio cue and the actual strike model classification result performed by the user must be compared based on the timestamp.
* Depending on the beat timing and motion accuracy, the strike judgment must be processed into 3 levels (Perfect, Good, Miss).

---

### 5. Multi-Sensory Feedback and Data Visualization

5.1 Ultra-Low Latency 3D Spatial Audio (Audio Feedback)
* As soon as a strike motion is recognized, the audio sound of the corresponding drum must be output within a latency (Latency) time of 50ms or less (Utilizing AAudio/Oboe library).
* Utilizing the head tracking data of the Galaxy Buds, a 3D spatial audio (Spatial Audio) effect must be applied so that the sense of direction of the sound changes according to the user's current head direction when the sound is output.

5.2 Macroscopic Visual Feedback and Tactile Feedback
* So that the user can intuitively know the judgment result even while swinging the smartphone, the color of the entire smartphone screen must be rendered to flash (Perfect=Green, Good=Orange, Miss=Red, etc.) at the moment of the strike.
* When an off-beat or incorrect motion (Miss judgment) is performed, strong haptic (vibration) feedback must be generated on the smartphone so that the user can recognize the incorrect answer without breaking immersion.

5.3 [For Evaluation/Developer] Real-time Sensor Data Visualization (Toggle)
* To prove the fulfillment of assignment requirements and system stability, real-time IMU sensor data of the smartphone, S-Pen, and Buds must be visualized and shown in the form of a line graph on one side of the screen.
* To prevent obstructing the player's view and wasting rendering resources, this visualization graph must be able to be turned on and off through a toggle (Toggle) switch on the UI.

---

### 6. Session Result Analysis Report

6.1 Comprehensive Statistics UI
* After the stage play ends, a report screen displaying whether the song was cleared, final score, maximum combo count, and the number of times by judgment (Perfect/Good/Miss ratio) must be provided.
* Statistical indicators that quantify and analyze the user's motion execution ability, such as the model's motion recognition accuracy (e.g., "Accuracy at Pattern 2 is 92%"), must be included.
