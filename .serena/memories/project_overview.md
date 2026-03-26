# FocusRitual — Project Overview

## Purpose
FocusRitual is a **premium ambient sound mixer + focus timer app** for Android and iOS.
It provides immersive ambient soundscapes that users can mix with independent volume sliders,
play/pause, and a dark "cinematic sanctuary" UI aesthetic.
A Pomodoro-style focus timer drives structured Focus/Break cycles with ambient sound integration.

## Current Status (March 2026)
- **Phase:** Core feature loop implemented (Mixer → Session Config → Active Timer)
- **What's built:**
  - Mixer screen with 9 ambient sounds, independent toggles + volume, play/pause
  - Dark forest background image with gradient overlay
  - Focus session configuration screen (presets + custom durations)
  - Active focus session timer with breathing circle animation, phase management, cycle progress
  - Audio playback via expect/actual AudioPlayer (Android: MediaPlayer, iOS: AVAudioPlayer)
  - SoundMixer orchestrator with master volume / session-aware playback
  - Phase-aware sound control: plays during Focus, silent during Break, fades on transitions
  - State-based navigation (Crossfade) between 3 screens
- **What's NOT built yet:** DI (Koin), persistence (DataStore), presets saving, settings screen
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
- **Navigation:** State-based (no navigation-compose library), AppScreen sealed interface + Crossfade

## Target Platforms
- **Android:** com.android.application plugin, MainActivity with edge-to-edge enabled
- **iOS:** Thin SwiftUI wrapper (iosApp/) embedding ComposeUIViewController via MainViewControllerKt

## Current Dependencies (in gradle)
- compose-runtime, compose-foundation, compose-material3, compose-ui
- compose.materialIconsExtended (for icons)
- compose-components-resources (shared resources — audio files, images)
- compose-uiToolingPreview
- androidx-lifecycle-viewmodelCompose (ViewModel for KMP)
- androidx-lifecycle-runtimeCompose (collectAsStateWithLifecycle)
- androidx-activity-compose (Android only)

## Planned Dependencies (not yet added)
- **DI:** Koin (koin-core, koin-compose, koin-compose-viewmodel) — add when ViewModels need injected deps
- **Persistence:** DataStore Preferences — add for settings/presets
- **Testing:** kotlinx-coroutines-test — add when timer logic needs unit tests
- **Networking:** Ktor — NOT needed until remote content/subscriptions

## Known Issues
- AGP deprecation warning: KMP plugin + com.android.application will require separation into
  library + app modules starting with AGP 9.0. No action needed yet.
