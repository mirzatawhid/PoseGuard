# PoseGuard

PoseGuard is an Android application that combines **real-time pose detection** with an **automated audio question flow**. Built using **CameraX**, **ML Kit**, **Jetpack Compose**, and **MVVM architecture**, the app provides interactive and intelligent user experiences.

---

## **Features**

### **Part 1 – Live Camera with Pose Detection**
- Open the device camera in real-time using **CameraX**.
- Detect poses using **ML Kit Pose Detection**.
- Overlay detected landmarks on the camera preview using **Canvas**.
- Track specific poses (e.g., raising both hands).
- Switch between **front** and **back** cameras seamlessly.

### **Part 2 – Automated Audio Question Flow**
- Navigate to a **Question Screen** after completing the pose screen.
- Supports at least **5 questions** with **2–3 static answer choices** each.
- Automatically plays question audio using **Text-to-Speech (TTS)**.
- Automatically starts **speech recognition** after playback.
- Matches user’s speech with available options and proceeds to the **next question automatically**.

---

## **Defined Pose**
Have to raise both your hand above nose.

---

## **Demo Video**
You can watch the PoseGuard demo video here:

- [Demo Video on Google Drive](https://drive.google.com/file/d/1CIu3BsmlENruNL50SH2Xq6-HikT5b1_L/view?usp=sharing)

---

## **Dependencies**
- AndroidX CameraX
- ML Kit Pose Detection
- Jetpack Compose
- Google Text-to-Speech
- Google Speech Recognizer

---

## **Build and Run the Project**

1. **Clone the repository**
```bash
git clone https://github.com/mirzatawhid/PoseGuard.git
```

1. **Open the project in Android Studio**
- Select **Open an Existing Project** and navigate to the project folder.

2. **Sync Gradle**
- Make sure all dependencies are downloaded. Click **Sync Now** if prompted.

3. **Grant Permissions**
- The app requires **Camera** and **Microphone** permissions for pose detection and speech recognition.

4. **Run on a Real Device**
- Pose detection requires a physical camera, so run the app on an Android device (emulators may not fully support ML Kit camera input).

5. **Build APK (Optional)**
- Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
- Install the APK on your device.

---

## **Release APK**

You can directly download the **latest release APK** from the link below to install PoseGuard without building from source:

- [Download PoseGuard APK with Demo](https://drive.google.com/drive/folders/1Ec-rLHEOT_RG6_h1sl21StAWo3-pYufH?usp=sharing)

> **Note:** Make sure to enable **installation from unknown sources** on your Android device before installing the APK.

---

## **How to Use**

### **Pose Detection Screen**
- Launch the app.
- Grant camera permission.
- Raise both hands to trigger the pose detection overlay.
- Switch between front and back cameras as needed.

### **Automated Question Flow**
- After finishing the pose screen, the app navigates to the question screen.
- Each question is played automatically via **TTS**.
- Speak one of the available options after audio playback.
- The app recognizes your answer and automatically moves to the next question.
- Complete all questions to finish the flow.

---

## **Future Improvements**
- Add **custom poses** for recognition.
- Integrate **dynamic question fetching** from a remote API.
- Provide **feedback or scoring** based on question answers and poses.
- Improve **UI/UX** for better accessibility.
