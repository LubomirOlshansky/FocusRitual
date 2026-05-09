# FocusRitual — Project Structure & Architecture

## Top-Level Layout

```
FocusRitual/
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/libs.versions.toml
├── composeApp/                          ← KMP + Compose shared module
│   └── src/{commonMain,androidMain,iosMain,commonTest}/kotlin/com/focusritual/app/
├── iosApp/                              ← SwiftUI host + WidgetKit/Live Activity extension
├── docs/                                ← living architecture docs
└── plans/                               ← implementation/refactor plans
```

## Top-Level Package Groups
Strict layer direction: `app/` → `feature/` → `core/`.

### Root (`commonMain`)
- `App.kt` — root composable, state-based `AppScreen` navigation via `Crossfade`, hoists `MixerViewModel`.
- `Platform.kt` — generic expect/actual platform metadata.

### `app/integration/`
Cross-feature glue that legitimately reads multiple feature states.
- `liveactivity/` — common Live Activity effect + platform actuals.

### `core/`
Cross-cutting platform infra + design system. `core/` must not import `feature/`.
- `core/audio/`
  - Common: `AudioCommand`, `AudioPlayer` expect, `AudioPlayerFactory`, `AudioPlayerHandle`, `OrganicMotionEngine`, `SoundMixer`, `AudioSettingsRepository`, `AudioPlaybackSettings`.
  - Android: `AudioPlayer.android.kt`, `AndroidAudioContext.kt`, `FocusAudioService.kt`, `AndroidAudioFocusController.kt`.
  - iOS: `AudioPlayer.ios.kt`, `IosAudioSessionController.kt`.
- `core/haptic/`
  - Common: `HapticFeedbackType`, `HapticEngine` expect, `HapticController`, `HapticSettingsRepository`.
  - Android/iOS: platform haptic engine actuals; Android also uses `AndroidHapticContext` initialized from `MainActivity`.
- `core/platformaction/`
  - Common: `PlatformActions`, `LocalPlatformActions`, `ProvidePlatformActions`, `rememberPlatformActions()` expect.
  - Android/iOS: actual providers plus `AndroidPlatformActions` / `IosPlatformActions`.
- `core/designsystem/`
  - `theme/`: color, type, spacing, motion, theme wiring.
  - `component/`: reusable controls such as play/start/close buttons, sliders, Protect Focus components.
- `core/protectfocus/` — expect/actual Screen Time / Focus protection bridge.
- `core/liveactivity/` (iOS) — Swift bridge-facing Live Activity infra.

### `feature/`
Canonical shape:
```
feature/<name>/
  <Name>Contract.kt
  <Name>ViewModel.kt
  <Name>Screen.kt or screen-level sheet/modal composable
  ui/
  domain/
    usecase/
  data/
```
Small features may omit folders until needed.

Current features:
- `feature/mixer/`
  - Root: `MixerContract`, `MixerViewModel`, `MixerScreen`, `CurrentMixModal`, `SaveMixDialog`.
  - `domain/`: `MixAudioOrchestrator`, `MixPreset`, `MixRepository`, DTOs/mappers/catalog/state, use cases.
  - `data/`: `JsonStore`, `MixPresetRepository`, `AmbientStateRepository` for persisted saved mixes and ambient snapshot.
  - `ui/` and `ui/modal/`: mixer surface and modal pieces.
- `feature/settings/`
  - Root: `SettingsContract`, `SettingsViewModel`, `SettingsModal` (currently in `SettingsSheet.kt`), `SettingsBackHandler` expect.
  - `domain/`: `SettingsRepository` facade over audio settings, `SoundCredit` data.
  - `ui/`: `SettingsHome`, `SettingsRows`, `SettingsDetails`, `SettingsSheetChrome`/`SettingsFrame`.
- `feature/session/`
  - Root: `FocusSessionContract`, `FocusSessionViewModel`, `FocusSessionScreen`, `SessionPreferences`.
  - `ui/`: session subcomponents.
- `feature/timer/`
  - Root: `ActiveSessionContract`, `ActiveSessionViewModel`, `ActiveSessionScreen`, `SessionCompleteScreen`.
  - `ui/`: atmospheric/session visual components.

Removed / replaced:
- `feature/about/` has been replaced by `feature/settings/`; sound credits now live under Settings.

## Entry Points
- Android: `MainActivity.onCreate()` → `ProvidePlatformActions { App() }`.
- iOS: `MainViewController()` → `ComposeUIViewController { ProvidePlatformActions { App() } }`.

## Architecture Notes
- Navigation is currently state-based (`AppScreen` sealed interface + `Crossfade`), not a true back stack.
- `MixerViewModel` is hoisted in `App.kt` so audio continues across session screens.
- Feature presentation follows MVI: `UiState`, `Intent`, `ViewModel`, single `onIntent()`.
- Platform side effects should flow through platform abstractions in `core/`; for feature actions, prefer ViewModel effects collected by composables rather than inline platform calls in UI.
- Swift-only APIs use the bridge pattern: Kotlin interface/protocol in `iosMain`, singleton bridge/controller, Swift implementation registered at app startup.

## Source Set Rules
| Code | Source Set |
|------|-----------|
| UI, ViewModels, models, repositories, pure domain | `commonMain` |
| Android Activity/Context/Service/audio focus | `androidMain` |
| iOS UIKit/AVFoundation/ActivityKit/Screen Time bridges | `iosMain` |
| Common tests and fakes | `commonTest` |

## Guardrails
- `core/` must not import `feature/`.
- `feature/<A>` must not import `feature/<B>`.
- Cross-feature code belongs in `app/integration/`.
- Platform actual files use `<Name>.<platform>.kt` suffix.
