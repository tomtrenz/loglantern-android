# LogLantern 🔦

LogLantern is an Android application that allows users to securely connect to a Splunk instance
and visualize log data using a mobile-friendly interface.

The application is built as a student project with a focus on modern Android development practices.

---

## 🎯 Project Goals

The main goal of this project is to create a mobile client for Splunk that:

- authenticates users via Splunk Management REST API
- securely stores an access/session token
- fetches data from Splunk using REST queries
- visualizes data as a table or a pie chart
- demonstrates MVVM architecture and Jetpack Compose UI

---

## 📱 Application Features

- Splash screen with authentication check
- Login screen for Splunk credentials
- Secure token storage (no password persistence)
- Data fetching via REST API
- Table view of results
- Pie chart visualization
- Settings screen with logout option

---

## 🧭 Application Screens

- Splash Screen
- Login Screen
- Dashboard
- Results Table Screen
- Results Chart Screen
- Settings Screen

---

## 🏗️ Architecture

The application follows the **MVVM (Model–View–ViewModel)** architecture:

- UI layer: Jetpack Compose
- State management: ViewModel + StateFlow
- Data layer: Repository pattern
- Networking: Retrofit + OkHttp

---

## 🧰 Tech Stack

- Kotlin
- Jetpack Compose
- Android MVVM
- Navigation Compose
- Retrofit
- OkHttp
- Encrypted local storage
- Git & GitHub

---

## 🔐 Security

- User credentials are used only for authentication
- Passwords are never stored
- Access token is stored securely
- Communication is done over HTTPS

---

## 📦 Build & Deployment

- The application can be built into an APK/AAB
- Runs on emulator or physical device
- Prepared for Google Play deployment

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

Tomáš Trenz  
Nick: mrthom  
Domain: splnsito.cz  
University: UTB Zlín
