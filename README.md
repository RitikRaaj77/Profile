# Profile App

Welcome to the Profile App, a sleek and modern Android application built using Jetpack Compose. This app features a visually stunning splash screen with animated falling lines, followed by a user profile screen that displays personal information, rewards, and transactions in a clean and organized layout. The app fetches user data from Firebase and ensures a seamless user experience with smooth animations and a dark theme.

---

## ✨ Features

- 🔵 **Stunning Splash Screen**: A beautiful splash screen with animated falling blue lines and a glowing effect, fading out smoothly to transition to the profile screen.
- 👤 **Profile Screen**: Displays user information such as name, member since date, credit score, lifetime cashback, bank balance, and rewards like cashback balance and coins.
- 🔥 **Firebase Integration**: Fetches user data in real-time from Firebase Realtime Database.
- 🎞️ **Smooth Animations**: Includes fade-in and fade-out transitions between the splash and profile screens, with a black background for a seamless experience.
- 📱 **Responsive Design**: Fully scrollable profile screen to ensure all content is accessible on smaller screens.
- 🌙 **Dark Theme**: A visually appealing dark theme with off-white text and subtle color accents for better readability.

---

## 📷 Screenshots

### Logo

<img src="./Nocap.png" alt="Profile App Logo" width="100"/>

> The Profile App logo, showcasing a minimalist design with a focus on user profiles.

### Splash Screen and Profile Screen

<img src="./profile2.png" alt="Splash Screen" width="300"/>  
<img src="./profile3.png" alt="Splash Screen 2" width="300"/>  
<img src="./profile1.png" alt="Profile Screen" width="300"/>

> The splash screen with animated falling lines (left) transitions to the profile screen (right), displaying user data and rewards.

---

## 🎥 Demo Video

> 📹 A quick demo of the Profile App, highlighting the splash screen animation and profile screen navigation.

<video src="./profileVideo.mp4" controls width="100%"></video>

---

## 📦 Download

Download the latest APK here:

[Download APK](./apk.apk)

---

## 🧑‍💻 Usage

- Displays after splash screen with a 1-second fade-in.
- Shows user data including credit score, cashback, bank balance, and rewards.
- Scroll to view "YOUR REWARDS & BENEFITS" and "TRANSACTIONS & SUPPORT".

---

## 🗂 Project Structure

| File                  | Description                                                                   |
|-----------------------|-------------------------------------------------------------------------------|
| `MainActivity.kt`     | Entry point of the app, handles navigation and splash/profile transitions.   |
| `SplashScreen.kt`     | Composable for the splash screen with animated lines and glowing text.       |
| `ProfileScreen.kt`    | Composable for displaying user data in a scrollable layout.                  |
| `ProfileViewModel.kt` | ViewModel to manage data fetching from Firebase.                             |
| `UserData.kt`         | Data class representing user information fetched from Firebase.              |

---
