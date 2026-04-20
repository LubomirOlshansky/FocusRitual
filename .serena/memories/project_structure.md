# FocusRitual — Project Structure & Architecture

## Top-Level Layout

```
FocusRitual/
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/libs.versions.toml            ← version catalog (all deps here)
├── composeApp/                          ← KMP + Compose shared module
│   └── src/{commonMain,androidMain,iosMain,commonTest}/kotlin/com/focusritual/app/
├── iosApp/                              ← SwiftUI host + FocusRitualWidget extension
├── docs/                                ← living architecture docs
└── plans/                               ← refactor/feature plans
```

## `com.focusritual.app` — Four Top-Level Groups

Post structure-refactor (plans/structure-refactor.md). **Strict layer direction: `app/` → `feature/` → `core/`.**

### Root (commonMain only)
- `App.kt` — root composable, `AppScreen` navigation via `Crossfade`
- `Platform.kt` — expect/actual platform interface

### `app/integration/` — multi-feature glue
Explicit home for code that legitimately spans multiple features.
- `app/integration/liveactivity/`
  - `LiveActivityEffect.kt` (commonMain, `expect`)
  - `LiveActivityEffect.ios.kt` (iosMain actual)
  - `LiveActivityEffect.android.kt` (androidMain actual — note `.android.kt` suffix, D5 of plan)

### `core/` — platform infra + design system (no `feature/` imports allowed)
- `core/audio/` — `AudioCommand`, `AudioPlayer` (expect), `AudioPlayerFactory`, `AudioPlayerHandle`, `OrganicMotionEngine`, `SoundMixer`
  - androidMain: `AudioPlayer.android.kt`, `AndroidAudioContext.kt`, `FocusAudioService.kt`
  - iosMain: `AudioPlayer.ios.kt`
- `core/designsystem/`
  - `theme/` — `Color`, `Type`, `Theme`, `Spacing`, `Motion`
  - `component/` — `AirPlayButton`, `CloseButton`, `PlayButton`, `ProtectFocusCard`, `ProtectFocusSetupSheet`, `StartSessionButton`, `StepperRow`, `VolumeSlider` (iOS/Android actuals per-platform)
- `core/protectfocus/` — `ProtectFocusContract`, `ProtectFocusController` (expect); iosMain: `ScreenTimeBridge`, `MockScreenTimeHandler`, `ProtectFocusController.ios.kt`
- `core/liveactivity/` *(iosMain only)* — pure platform infra: `LiveActivityBridge`, `LiveActivityController`, `LiveActivityState`. (The cross-feature **effect** lives under `app/integration/`.)

### `feature/` — user-facing features

**Canonical feature shape (D1 of plan):**
```
<Feature>Contract.kt        ← UiState + Intent
<Feature>ViewModel.kt
<Feature>Screen.kt          ← stateful + stateless split
ui/                         ← sub-composables (optional `ui/modal/`)
domain/                     ← models, repos, mappers, orchestrators, contracts
  usecase/                  ← single-responsibility use cases
data/                       ← persistence/network (none exist today)
```

**Features:**
- `feature/about/` — flat: `AboutSheet.kt`, `SoundCredit.kt`
- `feature/mixer/`
  - root: `MixerContract`, `MixerViewModel`, `MixerScreen`, `CurrentMixModal`
  - `ui/`: `CategoryPillRow`, `CurrentMixPanel`, `HeroSessionButton`, `ImmersiveBackground`, `SectionHeader`, `SoundTile`
  - `ui/modal/`: `ActiveSoundRow`, `DoneButton`, `GlobalOrganicMotionRow`, `ModalHeader`
  - `domain/`: `MixAudioOrchestrator`, `MixRepository`, `MixerDtos`, `MixerMappers`, `SoundCatalog`, `SoundCatalogImpl`, `SoundState`
  - `domain/usecase/`: 7 use cases (Adjust/Remove/SelectCategory/ToggleGlobalOrganicMotion/ToggleOrganicMotion/TogglePlayback/ToggleSound)
- `feature/session/`
  - root: `FocusSessionContract`, `FocusSessionViewModel`, `FocusSessionScreen`, `SessionPreferences`
  - `ui/`: `SessionModeToggle`
- `feature/timer/`
  - root: `ActiveSessionContract`, `ActiveSessionViewModel`, `ActiveSessionScreen`, `SessionCompleteScreen`
  - `ui/`: `AtmosphericField`, `SessionBackground`, `SessionControls`

### Platform entry points (source-set roots)
- androidMain: `MainActivity.kt`, `Platform.android.kt`
- iosMain: `MainViewController.kt`, `Platform.ios.kt`

## Layer Invariant (enforced)
- **`core/` must NOT import `feature/`.** Verified via grep across all three source sets.
- **`feature/` must NOT import from another `feature/`.**
- Cross-feature glue lives in `app/integration/` only.
- Platform actuals use `<Name>.<platform>.kt` suffix (`.ios.kt`, `.android.kt`).

## Architecture

### Navigation
- No nav library — `AppScreen` sealed interface in `App.kt` + `Crossfade(tween(300))`.
- `MixerViewModel` hoisted in `App.kt` and shared across screens for audio continuity.

### MVI (all features)
- `UiState` data class exposed via `StateFlow`
- `Intent` sealed interface (each action = `data object`/`data class`)
- `ViewModel` exposes `val uiState: StateFlow<UiState>` + single `fun onIntent(intent)`
- Screen split: stateful `<Feature>Screen(viewModel)` + stateless `<Feature>ScreenContent(uiState, onIntent)`

### Audio
- `AudioPlayer` expect/actual (Android: MediaPlayer+temp file; iOS: AVAudioPlayer)
- `SoundMixer.syncState(sounds, isPlaying, masterVolume)` — declarative sync
- `MixerViewModel` exposes `setSessionMasterVolume(Float?)` for session-aware fade
- `combine(_uiState, _sessionMasterVolume)` drives reactive playback

### Sound-Session Integration
- `ActiveSessionScreen` receives `onSoundControl: (Float?) -> Unit`
- Phase volume: Focus=1f, Break/Paused/Exiting=0f, with `animateFloatAsState(tween(400))`
- `DisposableEffect` restores `onSoundControl(null)` on exit

### Protect Focus (iOS Screen Time)
- Bridge pattern: Kotlin `ScreenTimeHandler` interface (iosMain, → ObjC `@protocol`) + `ScreenTimeBridge` singleton
- `ScreenTimeManager.swift` conforms and registers at app startup
- `ProtectFocusController` expect class; iOS actual uses `suspendCancellableCoroutine`
- State machine: `Idle → SheetOpen → SettingUp → SetupCompleted/Cancelled/PermissionDenied`

### Live Activity (iOS)
- Platform infra in `iosMain core/liveactivity/` (Bridge/Controller/State)
- Cross-feature driver in `commonMain app/integration/liveactivity/LiveActivityEffect.kt` (reads `MixerUiState` + `ActiveSessionUiState` + `SessionMode`)
- Android actual is a no-op stub

## Screen Flow
```
MixerScreen → FocusSessionScreen → ActiveSessionScreen
     ↑              ↑                      │
     └──────────────┴─────── onFinish ─────┘
```

## Source Set Rules
| Code | Source Set |
|------|-----------|
| UI, ViewModels, models, design system | commonMain |
| Audio actuals, platform APIs (actual) | androidMain / iosMain |
| Android-only (Activity, Context, Service) | androidMain |
| iOS-only (UIKit interop, Live Activity bridge, ScreenTime bridge) | iosMain |

## Entry Points
- Android: `MainActivity.onCreate()` → `setContent { App() }`
- iOS: `MainViewController()` → `ComposeUIViewController { App() }`

## Resources
- `composeResources/drawable/background.png`
- `composeResources/files/*.m4a` — 9 ambient sounds
