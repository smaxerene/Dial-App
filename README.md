# Dialler Application – Android (Kotlin + Jetpack Compose)

### Overview
This project is a simple dialler application for Android, written in Kotlin using Jetpack Compose. 
It replicates a standard phone dialler UI where users can input numbers, delete digits, and initiate phone calls.

The assignment requirements have been implemented to ensure correct behaviour across API level 26+ devices, landscape/portrait orientations, and different screen sizes.

### Features
- Dial Pad UI
  * Buttons for digits 0–9, * and #
  * Material Symbols Outlined icons:
    - backspace → delete last digit
    - call → place phone call
    - voicemail → optional voicemail button

- Input Handling
  * Pressing a digit appends it to the number.
  * Pressing delete (backspace) removes the last digit.

- Calling Functionality
  * Uses an Intent with ACTION_CALL and tel:<number>.
  * Runtime permission request for CALL_PHONE.

- Intent Handling
  * Accepts ACTION_DIAL intents.
  * Displays the number provided via URI.
  * Example test command (adb shell):
      `am start -a android.intent.action.DIAL -d "tel:094140800"`

- Rotation and Responsiveness
  * Fully functional in portrait and landscape modes.
  * Layout adapts for different screen sizes and orientations.

### Permissions
The app requires the following permission at runtime:
    `<uses-permission android:name="android.permission.CALL_PHONE" />`

When pressing the call button, the app will request permission if not already been granted.


### Testing
- Direct Call:
  Enter a number using the dial pad and press the call icon.

- ACTION_DIAL Intent:
  `adb shell am start -a android.intent.action.DIAL -d "tel:094140800"`
  The app should open with the number displayed.

- Rotation:
  Rotate the device/emulator between portrait and landscape while the app is open. 
  The UI and input should persist.
