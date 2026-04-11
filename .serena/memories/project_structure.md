# FocusRitual — Project Structure & Architecture

## Directory Layout

```
FocusRitual/                         ← Root (Gradle root project)
├── build.gradle.kts                 ← Root build file (no plugins applied directly)
├── settings.gradle.kts              ← Includes :composeApp, sets up repositories
├── gradle.properties                ← Kotlin/Gradle/Android properties
├── gradle/
│   ├── libs.versions.toml           ← Version catalog (ALL deps defined here)
│   └── wrapper/gradle-wrapper.properties
│
├── composeApp/                      ← Main shared module (KMP + Compose)
│   ├── build.gradle.kts             ← KMP plugin config, source sets, Android app config
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/com/focusritual/app/   ← All shared Kotlin code
│       │   └── composeResources/
│       │       ├── drawable/background.png   ← Dark forest background image
│       │       ├── drawable/compose-multiplatform.xml
│       │       └── files/*.m4a               ← 9 ambient sound files
│       │
│       ├── androidMain/kotlin/com/focusritual/app/
│       │   ├── MainActivity.kt     ← Android entry point, calls App()
│       │   ├── Platform.android.kt ← actual fun getPlatform()
│       │   └── core/audio/
│       │       ├── AndroidAudioContext.kt  ← Stores Application context for temp files
│       │       └── AudioPlayer.android.kt ← actual: MediaPlayer + temp file approach
│       │
│       └── iosMain/kotlin/com/focusritual/app/
│           ├── MainViewController.kt ← ComposeUIViewController { App() }
│           ├── Platform.ios.kt       ← actual fun getPlatform()
│           └── core/
│               ├── audio/
│               │   └── AudioPlayer.ios.kt ← actual: AVAudioPlayer
│               ├── designsystem/component/
│               │   ├── AirPlayButton.ios.kt       ← actual: AVRoutePickerView
│               │   ├── ProtectFocusCard.ios.kt     ← actual: entry row with shield icon
│               │   └── ProtectFocusSetupSheet.ios.kt ← actual: premium bottom sheet
│               └── protectfocus/
│                   ├── ScreenTimeBridge.kt          ← ScreenTimeHandler interface + singleton
│                   └── ProtectFocusController.ios.kt ← actual: suspendCancellableCoroutine → bridge
│
└── iosApp/                          ← iOS Xcode project (thin SwiftUI wrapper)
    ├── Configuration/Config.xcconfig
    ├── iosApp/
    │   ├── iOSApp.swift             ← @main SwiftUI App, registers ScreenTimeBridge
    │   ├── ContentView.swift        ← UIViewControllerRepresentable bridging to Compose
    │   ├── ScreenTimeManager.swift  ← FamilyControls auth + FamilyActivityPicker (Swift-only APIs)
    │   └── Info.plist
    └── iosApp.xcodeproj/            ← Xcode project files
```

## commonMain Source Tree

```
com/focusritual/app/
├── App.kt                          ← Root composable: AppScreen navigation via Crossfade
├── Platform.kt                     ← expect/actual platform interface
│
├── core/
│   ├── audio/
│   │   ├── AudioPlayer.kt          ← expect class: play(ByteArray), stop(), setVolume(), release(), isPlaying
│   │   ├── SoundResources.kt       ← Maps sound IDs → Compose resource paths
│   │   └── SoundMixer.kt           ← Orchestrates multiple AudioPlayers, syncState(sounds, isPlaying, masterVolume)
│   │
│   ├── designsystem/
│   │   ├── theme/
│   │   │   ├── Color.kt            ← 17 color tokens (dark palette incl. Outline)
│   │   │   ├── Type.kt             ← FocusRitualTypography (5 text styles, system font)
│   │   │   └── Theme.kt            ← FocusRitualTheme wrapping MaterialTheme with darkColorScheme
│   │   └── component/
│   │       ├── PlayButton.kt       ← Reusable 96dp circular glassmorphic play/pause button
│   │       ├── AirPlayButton.kt    ← expect/actual: iOS AirPlay route picker
│   │       ├── ProtectFocusCard.kt ← expect/actual: entry row on session screen
│   │       ├── ProtectFocusSetupSheet.kt ← expect/actual: premium bottom sheet (isSettingUp param)
│   │       ├── SoundTile.kt        ← Sound toggle tile
│   │       └── VolumeSlider.kt     ← Volume slider component
│   │
│   └── protectfocus/
│       ├── ProtectFocusContract.kt  ← ProtectFocusState + SetupResult sealed interfaces
│       └── ProtectFocusController.kt ← expect class: suspend requestSetup() → SetupResult
│
└── feature/
    ├── mixer/
    │   ├── MixerContract.kt        ← MixerUiState + MixerIntent (MVI contract)
    │   ├── MixerViewModel.kt       ← ViewModel: SoundMixer owner, session volume control, audio resource loading
    │   ├── MixerScreen.kt          ← MixerScreen (stateful) + MixerScreenContent (stateless) + ImmersiveBackground
    │   └── model/
    │       └── SoundState.kt       ← SoundState data class + SoundIcon enum + defaultSounds()
    │
    ├── session/
    │   ├── FocusSessionContract.kt ← SessionPreset, FocusSessionUiState, FocusSessionIntent, SessionConfig
    │   ├── FocusSessionViewModel.kt← ViewModel: preset selection, custom adjustments, resolveConfig()
    │   └── FocusSessionScreen.kt   ← FocusSessionScreen (stateful) + FocusSessionScreenContent (stateless)
    │
    └── timer/
        ├── ActiveSessionContract.kt ← SessionPhase enum (Focus/Break), ActiveSessionUiState, ActiveSessionIntent
        ├── ActiveSessionViewModel.kt← Timer: coroutine countdown, phase/cycle management, pause/skip/stop
        └── ActiveSessionScreen.kt   ← ActiveSessionScreen + immersive timer UI (breathing circle, progress dots, controls)
```

## Architecture

### Navigation
- **No navigation library** — state-based routing in App.kt
- `AppScreen` sealed interface: `Mixer`, `FocusSession`, `ActiveSession(config: SessionConfig)`
- `Crossfade(tween(300))` transitions between screens
- MixerViewModel is hoisted in App.kt and shared across screen transitions for audio continuity

### MVI Pattern (all features)
- **State:** Data class with defaults, exposed via `StateFlow`
- **Intent:** Sealed interface, each action is a `data object` or `data class`
- **ViewModel:** Extends `ViewModel()`, exposes `val uiState: StateFlow<UiState>`, single `fun onIntent(intent)`
- **Screen split:** Stateful composable (owns/receives ViewModel, collects state) + Stateless content

### Audio Architecture
- `AudioPlayer` — expect/actual: play(ByteArray), stop(), setVolume(Float), release(), isPlaying
  - Android: MediaPlayer + temp file
  - iOS: AVAudioPlayer
- `SoundMixer` — manages map of AudioPlayers per sound ID
  - `syncState(sounds, isPlaying, masterVolume)` — declarative sync of UI state to playback
  - `masterVolume` parameter enables session-aware volume fade (1.0 = full, 0.0 = silent)
- `MixerViewModel` — owns SoundMixer, loads audio resources, provides `setSessionMasterVolume(Float?)`
  - `null` = mixer controls playback normally (user toggle)
  - `Float` (0..1) = session overrides playback (Focus=1, Break=0, with animated fade)
  - Uses `combine(_uiState, _sessionMasterVolume)` for reactive sync

### Sound-Session Integration
- `ActiveSessionScreen` receives `onSoundControl: (Float?) -> Unit` callback
- Phase-based volume: Focus → 1f, Break → 0f, Paused → 0f, Exiting → 0f
- `animateFloatAsState(tween(400))` provides smooth fade transitions
- `DisposableEffect` ensures `onSoundControl(null)` on composition exit (releases session control)
- Session exit uses `isExiting` flag → fade-out animation → then `onFinish()` navigation

### Protect Focus (iOS-only Screen Time blocking)
- **Bridge pattern** for Swift-only FamilyControls APIs (not @objc, can't call from Kotlin/Native)
- `ScreenTimeHandler` — Kotlin interface in iosMain, compiles to ObjC @protocol
- `ScreenTimeBridge` — Kotlin object singleton, holds `handler: ScreenTimeHandler?`
- `ScreenTimeManager.swift` — Swift class conforming to ScreenTimeHandler, uses AuthorizationCenter + FamilyActivityPicker
- Registered at app startup: `ScreenTimeBridge.shared.handler = ScreenTimeManager()` in iOSApp.swift
- `ProtectFocusController` — expect/actual class, iOS actual uses `suspendCancellableCoroutine` to bridge callbacks → coroutine
- **State machine** in FocusSessionScreenContent: `ProtectFocusState` (Idle → SheetOpen → SettingUp → SetupCompleted/Cancelled/PermissionDenied)
- CTA disables (alpha dim) while `SettingUp`, native picker covers the sheet

### Screen Flow
```
MixerScreen → FocusSessionScreen → ActiveSessionScreen
     ↑              ↑                      │
     │              │                      │ (onFinish)
     └──────────────┴──────────────────────┘
```

## Source Set Rules

| Code Type | Source Set |
|-----------|-----------|
| UI (Composables) | commonMain |
| Business logic / ViewModels | commonMain |
| Data models | commonMain |
| Theme / design system | commonMain |
| Audio playback (AudioPlayer actual) | androidMain / iosMain |
| Platform APIs | expect in commonMain, actual in androidMain/iosMain |
| Android-only (Activity, Context) | androidMain |
| iOS-only (UIKit interop) | iosMain |

## Entry Points
- **Android:** `MainActivity.onCreate()` → `setContent { App() }`
- **iOS:** `MainViewController()` → `ComposeUIViewController { App() }`
- **App.kt:** `FocusRitualTheme { Crossfade(AppScreen) { ... } }`

## Resources
- **Background:** `composeResources/drawable/background.png` — dark forest, accessed via `Res.drawable.background`
- **Audio:** 9 `.m4a` files in `composeResources/files/` — rain, wind, fire, birds, waves, stream, thunder, cafe, night
