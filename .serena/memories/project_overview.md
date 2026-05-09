# FocusRitual — Project Overview

## Purpose
FocusRitual is a premium ambient sound mixer + focus timer app for Android and iOS.
It provides immersive ambient soundscapes with independent volume sliders, saved mixes, focus/sleep session flows, and a dark cinematic sanctuary UI.

## Current Status (May 2026)
- Phase: Core loop plus Settings and persistence implemented.
- Built:
  - Mixer screen with 9 bundled ambient sounds, independent toggles, volumes, organic motion, play/pause, current-mix modal, save-mix dialog, and persisted saved mixes/ambient state.
  - Focus and sleep session configuration with persisted custom durations.
  - Active focus/sleep timer with phase management, breathing visual field, cycle progress, pause/skip/end controls, and phase-aware audio volume.
  - Audio playback via expect/actual `AudioPlayer` (Android `MediaPlayer`, iOS `AVAudioPlayer`) coordinated by `SoundMixer` and `MixAudioOrchestrator`.
  - Shared audio settings (`mixWithOthers`, `duckOthers`, mix volume) persisted with `multiplatform-settings` and wired into Android audio focus + iOS `AVAudioSession`.
  - Draggable Settings modal overlay replacing the old About sheet/full-screen Settings route: audio preferences, app actions, support/contact, sound credits, privacy policy, terms of use.
  - Platform actions layer for language settings, rate/review, share, email, URL opening, and app version, driven from Settings one-shot effects.
  - iOS native integrations: Live Activities and Protect Focus / Screen Time bridge skeleton.
  - State-based app navigation with `AppScreen` + `Crossfade`; no navigation library yet.
- Not built / incomplete:
  - Dependency injection (Koin still deferred).
  - Production legal copy/translation review and final App Store ID.
  - Manual verification for app-language settings and iOS edge-swipe behavior.
  - Production-ready Protect Focus entitlement/device flow.

## Tech Stack
- Language: Kotlin 2.3.0
- UI: Compose Multiplatform 1.10.0, Material 3 (`compose-material3` 1.10.0-alpha05)
- Architecture: MVI presentation layer with `StateFlow` + lifecycle ViewModel Compose
- Persistence: `multiplatform-settings-no-arg`; mixer persistence uses `kotlinx-serialization-json`
- Build: Gradle Kotlin DSL + version catalogs
- Targets: Android API 28+ / compileSdk 36, iOS arm64 + simulator arm64; app minimum iOS target is 18+
- Package: `com.focusritual.app`

## Target Platforms
- Android: `com.android.application`, `MainActivity`, edge-to-edge, foreground audio service.
- iOS: SwiftUI host embedding `ComposeUIViewController`, WidgetKit/ActivityKit extension, AppIntents/Live Activity support.

## Current Dependencies
- Compose runtime/foundation/material3/ui/resources/icons/tooling preview
- AndroidX lifecycle ViewModel + runtime Compose
- AndroidX activity-compose (Android only)
- `multiplatform-settings-no-arg`
- `kotlinx-serialization-json` + Kotlin serialization plugin

## Planned / Deferred Dependencies
- Koin for DI when constructor chains become painful.
- A navigation library only if state-based navigation becomes too limiting.
- Ktor only for future remote content/subscriptions/analytics.

## Known Issues
- AGP 9 will eventually require splitting Android application and KMP module responsibilities.
- Settings is now a draggable modal overlay with top-right close, Settings effects, first-pass shared localization resources, Android/iOS locale declarations, and iOS-first edge swipe-back are implemented; remaining risk is device/manual verification plus translation/legal review.
