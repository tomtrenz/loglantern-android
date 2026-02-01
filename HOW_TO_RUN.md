# LogLantern Android App

A Splunk log viewing application built with Kotlin, Jetpack Compose, MVVM architecture, and Hilt for dependency injection.

## Architecture

- **Single Activity Architecture**: MainActivity only
- **MVVM Pattern**: Each feature has its own ViewModel and UiState
- **Dependency Injection**: Hilt
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Navigation Compose
- **Data Storage**: DataStore for preferences, encrypted token storage

## Project Structure

```
cz.splnsito.mrthom.loglantern/
├── app/
│   ├── LogLanternApplication.kt   # Application class with Hilt
│   ├── LogLanternApp.kt            # Main Compose entry point
│   ├── MainActivity.kt             # Single activity
│   └── di/                         # Hilt modules
│       ├── AppModule.kt
│       ├── NetworkModule.kt
│       └── StorageModule.kt
├── core/
│   ├── network/
│   │   └── auth/                   # Authentication interceptors
│   ├── security/                   # Token storage
│   └── ui/
│       ├── components/             # Shared UI components
│       └── theme/                  # Material 3 theme
├── data/
│   └── local/                      # Local data sources
├── feature/
│   ├── splash/                     # Splash screen
│   ├── auth/                       # Login screen
│   ├── dashboard/                  # Dashboard
│   ├── settings/                   # Settings screen
│   └── results/
│       ├── table/                  # Results table view
│       └── chart/                  # Results pie chart view
└── navigation/                     # Navigation graph

```

## Features

### Implemented
- **Splash Screen**: Checks for authentication token and navigates appropriately
- **Login**: Stores Splunk base URL and authentication token
- **Dashboard**: Navigation hub to other features
- **Settings**: View base URL and logout functionality
- **Results Table**: Placeholder for table view
- **Results Pie Chart**: Placeholder for chart view

### Navigation Flow
```
Splash → (if token exists) → Dashboard
       → (if no token) → Login → Dashboard
```

## Dependencies

- Jetpack Compose with Material 3
- Hilt for dependency injection
- Navigation Compose
- DataStore for preferences
- Security Crypto for encrypted storage
- OkHttp & Retrofit for networking
- Lifecycle Runtime Compose for state collection

## How to Build

### Prerequisites
- JDK 11 or higher
- Android SDK with API level 34
- Internet access to Google Maven repository (dl.google.com)

### Build Commands

```bash
# Clean build
./gradlew clean

# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

### Troubleshooting

**Network Issues**: If you encounter errors accessing Google's Maven repository (dl.google.com), ensure:
1. You have internet connectivity
2. No firewall is blocking dl.google.com
3. No proxy configuration is interfering

**Build Errors**: 
- Ensure Android SDK is properly installed
- Verify ANDROID_HOME or ANDROID_SDK_ROOT environment variable is set
- Run `./gradlew --refresh-dependencies` to clear cached dependencies

## Security Notes

- Passwords are NOT stored locally
- Only authentication tokens and base URLs are persisted
- Token storage uses DataStore (can be upgraded to EncryptedSharedPreferences if needed)

## Future Enhancements

- Implement actual Splunk API integration
- Add real data visualization for table and pie chart views
- Implement search functionality
- Add error handling and retry logic
- Implement proper logout flow with token revocation

## License

See LICENSE file for details.
