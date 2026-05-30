# 04_UI_UX_Design

### Version History

| Version | Date | Remarks |
| --- | --- | --- |
| 1.0 | 2026-05-14 | Initial release |
| 1.1 | 2026-05-14 | Updated stage selection and simplified play screen |
| 1.2 | 2026-05-18 | Updated for 8-axis system and removed spatial audio |
| 1.3 | 2026-05-30 | Updated for parallel dual-model architecture and removed calibration phase |

### 1. Design Principles: Non-Visual Dominance

- Visual Subordination: Since the user cannot read detailed text on the screen while swinging the device, color, haptic, and audio are used as the primary feedback methods.
- Simplicity: Minimize complex button operations and screen elements to focus on the essential rhythm of the game.

### 2. Main Screen Flow

| Order | Screen Name | Key Features and UI Elements |
| --- | --- | --- |
| 01 | Main Home | Game start button, settings (difficulty, vibration, sound), smartphone and S-Pen connection status icon. |
| 02 | Stage Selection | Horizontal scroll type stage list, free mode entry button at the top right. |
| 03 | Game Play | Full screen color feedback, current and next pattern display, real-time sensor graph toggle. |
| 04 | Result Report | Final score, judgment statistics (perfect, good, miss), recognition accuracy analysis. |

### 3. Detailed UI Design

3.1. Main Home and Settings

- Difficulty Settings: Managed within the settings menu, choosing the strictness of the accuracy tolerance (easy, normal, hard).
- Function: As the difficulty increases, the AI model narrows the tolerance range for hit timing and trajectory.

3.2. Stage Selection Screen

- Horizontal Scroll Layout: Stages are arranged in a line from left to right.
- Stage Configuration Example (Stage 1):
  - 1-1, 1-2, 1-3 (Practice): Stages to learn specific patterns individually. Each step includes specific sound sources for the pattern.
  - 1-Main: A stage where all learned patterns appear to complete the full song.
  - Free Play: A fixed button at the top right of the screen for unrestricted play at any time.

3.3. Game Play Screen

- Core Feedback Elements:
  1. Background Color Feedback: The entire screen flashes green (perfect), orange (good), or red with vibration (miss) based on the judgment. Supports simultaneous visual feedback for cross-hand drum strikes.
  2. Pattern Guide: The current pattern and the next pattern are displayed concisely at the top with icons or text.
  3. Sensor Toggle: A button to turn the separated real-time sensor graphs (smartphone 6-axis and S-Pen 2-axis) on and off.

### 4. Sound and Haptic Design

- Audio Cue: Lowers visual dependence by providing voice instructions before the start of a measure (e.g., Pattern 3).
- Low-Latency Audio Feedback: Provides immediate multi-channel mixing audio feedback for the corresponding drums upon parallel strike recognition without fusion delay.
- Vibration Pattern: Provides short and strong vibration on miss, and long congratulatory vibration on clearing a song.
