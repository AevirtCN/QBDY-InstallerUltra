# QBDY Installer Ultra

A powerful Android application management tool, supporting app installation, app management, and system tool functions.

## Feature Highlights

### 📦 App Installation
- Support selecting APK files from file browser for installation
- Fast installation of Android apps

### ⚙️ App Management
- View and manage installed applications
- Application list management features

### 🔧 Toolbox
- Device manager permission management
- Request all files access permission (for Android 11+)
- File browser functionality

## Technical Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: Android 6.0 (API 23)
- **Target SDK**: Android 13 (API 33)

## Core Components

### MainActivity
Main entry point of the app, containing the following core functions:
- `InstallTab` - App installation tab
- `AppManageTab` - App management tab  
- `ToolsTab` - Toolbox tab
- `ActionCard` - Functional card component
- `GlassCard` - Frosted glass effect card component
- `requestAllFilesAccess()` - Request all files access permission
- `showFileBrowser()` / `browseDirectory()` - File browsing functionality

## Permission Description

The app requires the following permissions:
- Device manager permission (for app management features)
- File access permission (for installing APKs and file browsing)

## Installation

1. Clone the project:
```bash
git clone https://gitee.com/aevirtcn/qbdy-installer-ultra
```

2. Open the project using Android Studio

3. Build debug APK:
```bash
./gradlew assembleDebug
```

4. Install the generated APK on the device

## Usage Instructions

1. **App Installation**: Switch to the installation tab, click the functional card to select an APK file for installation
2. **App Management**: Switch to the management tab, view and manage installed applications
3. **Toolbox**: Switch to the tools tab, access device manager and file browsing features

## Project Structure

```
app/src/main/java/com/dykt/installerultra/
└── MainActivity.kt          # Main interface and core logic
```

## License

This project is for learning and research purposes only.