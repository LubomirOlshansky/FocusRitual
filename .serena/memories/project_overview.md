# FocusRitual — Project Overview

## Purpose
FocusRitual is a **premium ambient sound mixer + focus timer app** for Android and iOS.
It provides immersive ambient soundscapes that users can mix with independent volume sliders,
play/pause, presets, and a dark "cinematic sanctuary" UI aesthetic.
A Pomodoro timer feature is planned for future iterations.

## Current Status (March 2026)
- **Phase:** Initial vertical slice complete
- **What's built:** Mixer screen with play/pause button, dark immersive background, title/subtitle display
- **What's NOT built yet:** Sound sliders, real audio playback, timer, presets, settings, navigation, persistence
- **Build status:** Compiles and runs on both Android and iOS

## Tech Stack
- **Language:** Kotlin 2.3.0
- **UI Framework:** Compose Multiplatform 1.10.0 (JetBrains)
- **Design System:** Material 3 (compose-material3 1.10.0-alpha05)
- **Architecture:** MVI (Model-View-Intent) for presentation layer
- **State Management:** StateFlow + ViewModel (lifecycle-viewmodel-compose 2.9.6)
- **Build System:** Gradle with Kotlin DSL + version catalogs (gradle/libs.versions.toml)
- **Targets:** Android (API 28+, compileSdk 36) + iOS (iosArm64, iosSimulatorArm64)
- **Package:** `com.focusritual.app`

## Target Platforms
- **Android:** com.android.application plugin, MainActivity with edge-to-edge enabled
- **iOS:** Thin SwiftUI wrapper (iosApp/) embedding ComposeUIViewController via MainViewControllerKt

## Current Dependencies (in gradle)
- compose-runtime, compose-foundation, compose-material3, compose-ui
- compose.materialIconsExtended (for play/pause icons)
- compose-components-resources (shared resources)
- compose-uiToolingPreview
- androidx-lifecycle-viewmodelCompose (ViewModel for KMP)
- androidx-lifecycle-runtimeCompose (collectAsStateWithLifecycle)
- androidx-activity-compose (Android only)

## Planned Dependencies (not yet added)
- **Navigation:** org.jetbrains.androidx.navigation:navigation-compose — add when building 2nd screen
- **DI:** Koin (koin-core, koin-compose, koin-compose-viewmodel) — add when ViewModels need injected deps
- **Serialization:** kotlinx-serialization-json — add with navigation (type-safe routes)
- **Persistence:** DataStore Preferences — add for settings/presets
- **Audio:** Custom expect/actual AudioPlayer abstraction — add for real sound playback
- **Networking:** Ktor — NOT needed until remote content/subscriptions
- **Testing:** kotlinx-coroutines-test — add when timer logic is built

## Known Issues
- AGP deprecation warning: KMP plugin + com.android.application will require separation into
  library + app modules starting with AGP 9.0. No action needed yet.

## Gradle Configuration
- Kotlin code style: official
- Configuration cache: enabled
- Gradle caching: enabled
- Kotlin daemon JVM args: -Xmx3072M
- Gradle JVM args: -Xmx4096M
- Android: nonTransitiveRClass=true, useAndroidX=true
