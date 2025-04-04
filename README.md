# DLBAIPEAI – Age, Gender, and Expression Recognition on Edge Devices

An edge-deployable deep learning application that predicts **Age Group**, **Gender**, and **Facial Expression** in real time using a mobile device. Built using **PyTorch**, **EfficientNetV2**, and deployed via **Android Studio** for fast and private offline inference.

---

## 📱 Features

- 📸 Real-time face detection and recognition on mobile devices
- 🧠 Multi-head deep learning model with shared EfficientNetV2 backbone
- 🎭 Recognizes:
  - **Age Group**: `Baby`, `Child`, `Young Adult`, `Adult`, `Elderly`
  - **Gender**: `Male`, `Female`
  - **Expression**: `Happy`, `Sad`, `Neutral`, `Angry`, `Surprised`, `Disgusted`, `Fearful`
- ⚡ Optimized for edge devices (runs offline, no cloud dependency)

---

## 🧠 Model Architecture

A multi-task classification model with shared representation:

- **Backbone**: `EfficientNetV2` for feature extraction
- **Classification Heads**:
  - Age Group (5 classes)
  - Gender (2 classes)
  - Emotion (7 classes)

This approach allows efficient learning from shared facial features while outputting multiple predictions.

---

## Deploy on Android
Open DLBAIPEAI in Android Studio.
Place the .ptl model inside the app/src/main/assets/ folder.
Connect your Android phone or emulator and run the app.

## 🔧 Requirements
Python Side
- Python 3.8+
- PyTorch
- TorchVision
- NumPy
- OpenCV

Android Side
- Android Studio (Arctic Fox or newer)
- Java 8+
- Gradle
- Android device or emulator

## 📊 Datasets Used
- Age Classification: Custom-labeled dataset categorized into: Baby, Child, Young Adult, Adult, Elderly
- Gender: Biggest Gender Face Recognition Dataset
- Expression: FER-2013 / Facial Emotion Dataset
---
## 👩‍💻 Author
**Fotimakhon Gulomova**

🎓 AI Student at IU International University
- 🌐 [GitHub](https://github.com/fatimagulomova)
- 🔗 [LinkedIn](https://www.linkedin.com/in/fatima-gulamova/)
- 🔗 [Kaggle](https://www.kaggle.com/fotimakhongulomova/)

## 📜 License
This project is licensed under the MIT License.
Feel free to use, modify, and contribute.
