# AGENTS.md

This document provides essential information for AI agents working on the Iconify project.

## 1. Project Overview
Iconify is a free and open-source Android customization application designed for rooted Pixel and AOSP-based devices. It allows users to modify various aspects of the system UI (colors, shapes, icons, status bar, etc.) using a combination of **Android Overlays (RRO)** and **Xposed Hooks**.

## 2. Architecture Summary
The project is a single-module Android application (`app/`) with three main runtime surfaces:

-   **Compose UI (`features/**`)**: The frontend built with Jetpack Compose. It uses a custom `PreferenceScreen` DSL for building settings pages.
-   **Root/Service Glue (`services/**`, `core/utils/**`)**: Logic that interacts with the system using `libsu` for root shell access and AIDL services for cross-process communication.
-   **Xposed Hooks (`xposed/**`)**: Runtime hooks that target the `android` (framework) and `com.android.systemui` packages.

### Key Components:
-   **`MainActivity`**: Boots a `libsu` root shell and sets up `AppProviders`.
-   **`AppProviders`**: Wires `CompositionLocal`s (NavController, Settings, ColorScheme, Haptics, Density, etc.) consumed by screens.
-   **Preference System**: Managed by `PreferenceController` (`core/preferences/`). Keys are enums (`data/keys/`) implementing the `Key` interface.
-   **Remote Preferences**: `RemotePrefProvider` allows the Xposed process (SystemUI/Framework) to read preferences from the app's device-protected storage.
-   **Xposed Registry**: `EntryList.kt` is the authoritative list of all active Xposed mod packs.

## 3. Setup Instructions
-   **Environment**: Requires JDK 21 and Android SDK (Compile SDK 36).
-   **Root Requirement**: Testing requires a rooted device (Magisk/KernelSU/APatch) or an emulator with root.
-   **Signing**: Create a `keystore.properties` in the root directory (see `app/build.gradle.kts` for expected fields: `keyAlias`, `keyPassword`, `storeFile`, `storePassword`).
-   **Dependencies**: Uses Hilt for DI, Room for database, and KSP for annotation processing.

## 4. Common Commands
-   **Build Debug**: `.\gradlew.bat assembleDebug` (Appends `.debug` to application ID).
-   **Build Release**: `.\gradlew.bat assembleRelease`.
-   **Rename APKs**: `.\gradlew.bat renameApks` (Renames build outputs to a standardized format).
-   **Clean Project**: `.\gradlew.bat clean`.

## 5. Testing Strategy
-   **Automated Tests**: There are currently no unit or instrumentation tests checked into the repository.
-   **Validation**: Must be performed manually by:
    1.  Successful build (`assembleDebug`).
    2.  Installing on a rooted device.
    3.  Granting root permissions.
    4.  Enabling Xposed module (if testing hooks).
    5.  Tracing logs via `logcat` (look for `Iconify` or `Xposed` tags).

## 6. Code Style / Conventions
-   **UI State**: Prefer reading state from `LocalSettings`, `LocalColorScheme`, or `LocalPreferenceController` rather than passing parameters down through multiple levels.
-   **DI**: Use Hilt for all dependency injection. ViewModels should be `HiltViewModel`.
-   **Preferences**:
    -   All preference keys must be added to the appropriate enum in `data/keys/`.
    -   Color preferences must be stored as **hex strings** (e.g., `"#8a51f5"`), not integers.
-   **Xposed Hooks**:
    -   Extend `ModPack` (`xposed/ModPack.kt`).
    -   Register new hooks in `EntryList.kt`.
    -   Always check `HookEntry.isChildProcess` to avoid hooking child processes if not needed.

## 7. Agent Rules (IMPORTANT)

### What NOT to change:
-   **`RemotePrefProvider` and `XPrefs` logic**: These are critical for cross-process communication between the app and Xposed.
-   **Package ID logic in `customize.sh`**: The Magisk module depends on specific package naming conventions.
-   **Child Process Checks**: Do not remove `!HookEntry.isChildProcess` checks in `EntryList` unless you specifically intend to hook child processes (high risk of performance issues).

### Safe Modifications:
-   Adding new UI screens or components using the `PreferenceScreen` DSL.
-   Adding new preference keys to existing enums.
-   Improving styling and micro-animations in the Compose UI.

### High-Risk Modifications:
-   **Xposed Hooks**: Incorrect hooks in `android` or `com.android.systemui` can cause **bootloops**. Always verify hook logic carefully.
-   **Root Commands**: Be extremely careful with `Shell.cmd(...)` as it runs with elevated privileges.

## 8. Known Pitfalls / Warnings
-   **ProGuard/R8**: Preference key enums (`data/keys/`) must be protected from obfuscation to ensure stored values remain accessible. Check `proguard-rules.pro` if adding new key classes.
-   **SDK 36**: The project targets Android 16 (SDK 36). Some APIs might be unstable or require specific platform signatures.
-   **Magisk Module**: The app generates a ROM-specific Magisk module. Changes to the directory structure in `ModuleBase/` must be reflected in the generation logic.
-   **Hex Colors**: Storing colors as hex strings is a legacy choice; parsing them incorrectly (e.g., as `Int`) will fail. Use `Color.parseColor()` or `toLong()`.
