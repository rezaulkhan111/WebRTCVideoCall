# 📱 Android Calling App (WebRTC + Firebase FCM)

This Android application enables **Voice & Video Calling** between users using **WebRTC** for peer-to-peer media streaming and **Firebase Cloud Messaging (FCM)** for call signaling & push notifications.

---

## 🚀 Features
- 🔔 Receive **Incoming Call Notifications** via FCM
- 🎥 **Video Call** support with local & remote video rendering
- 🎤 **Audio Call** only mode
- 📡 **Firebase Realtime Database** for live call status updates (call end, switch audio/video)
- ⏱️ Call timeout handling (auto cancel if unanswered)
- ✅ Works with ASP.NET MVC backend or Node.js signaling server [ASP.Net Core API](https://github.com/rezaulkhan111/WebRtcNotification)

---

## 📌 Tech Stack
- **Language**: Kotlin (Android)
- **Signaling**: Firebase Realtime Database
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Media Engine**: WebRTC (org.webrtc)
- **Architecture**: MVVM + ViewModel + LiveData/Flow

---

## 🔧 Setup Instructions

### 1. Firebase Setup
1. Create a project in [Firebase Console](https://console.firebase.google.com/).
2. Add your Android app (`package name`) to Firebase.
3. Download `google-services.json` and place it inside:
4. Enable **Cloud Messaging** and **Realtime Database** in Firebase.

### 2. Add Dependencies
In `app/build.gradle`:
```gradle
dependencies {
 // Firebase
 implementation 'com.google.firebase:firebase-messaging:23.0.8'
 implementation 'com.google.firebase:firebase-database:20.2.2'

 // WebRTC
 implementation 'org.webrtc:google-webrtc:1.0.+'

 // Lifecycle / ViewModel
 implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
 implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'
}
