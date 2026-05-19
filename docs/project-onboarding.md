# FocusRitual — Project Onboarding Overview

## What the App Does

FocusRitual is an **ambient focus and sleep companion**. It combines a sound mixer with Pomodoro-style focus timers and a sleep mode. The core loop:

1. User opens the app → inside an immersive mixer with ambient sounds playing
2. Taps a hero button → configures a focus or sleep session
3. Session runs → countdown timer, app blocking via Screen Time, live lock-screen widget
4. Session ends → returns to mixer

**Platforms:** Android + iOS via Kotlin Multiplatform + Compose Multiplatform. iOS gets additional native integrations (Live Activity, Screen Time/FamilyControls) via a Swift↔KMP bridge layer.

---

## Architecture

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Shared UI + business logic | Kotlin Multiplatform (KMP) + Compose Multiplatform |
| Android host | Jetpack / Gradle |
| iOS host | Swift + SwiftUI (ContentView wraps the KMP Compose surface) |
| iOS Live Activity | Swift Widget Extension + ActivityKit |
| iOS Screen Time | Swift `ScreenTimeManager` + FamilyControls |
| iOS→KMP bridge | `expect/actual` + singleton handler protocol pattern |
| Audio | Custom `AudioPlayer` `expect/actual` per platform |
| Navigation | No library — `sealed interface AppScreen` + `AnimatedContent` in `App.kt` |
| State management | MVI: `UiState` data class + `Intent` sealed interface + `ViewModel` with `StateFlow` |

### Module / Source Set Layout

```
composeApp/
  src/
    commonMain/      ← All shared UI, VMs, models, use cases, audio engine
    iosMain/         ← iOS-specific actual implementations (audio, haptic, live activity, protect focus)
    androidMain/     ← Android-specific actual implementations
iosApp/
  iosApp/            ← Swift main app (ContentView, bridges: LiveActivityManager, ScreenTimeManager)
  FocusRitualWidget/ ← Widget Extension (Live Activity views, FocusRitualAttributes, RitualTokens)
```

### Package Structure (commonMain)

```
com.focusritual.app
├── App.kt                          ← Root composable, navigation state machine
├── app/
│   ├── navigation/EdgeSwipeBackHandler.kt
│   └── integration/liveactivity/   ← App-level Live Activity coordination
├── core/
│   ├── audio/                      ← SoundMixer, AudioPlayer (expect/actual), OrganicMotionEngine
│   ├── designsystem/               ← Theme, Color.kt, Type.kt, Shape.kt
│   ├── haptic/                     ← HapticEngine (expect/actual), HapticController, types
│   ├── platformaction/             ← Platform-specific action bridge
│   ├── protectfocus/               ← ProtectFocusController (expect/actual), contract
│   └── util/
└── feature/
    ├── mixer/                      ← Mixer screen + all sub-components
    ├── session/                    ← Focus Session Setup screen
    ├── timer/                      ← Active Session (timer) screen
    └── settings/
```

### MVI Pattern (used throughout)

Every screen follows this structure:

- `<Feature>Contract.kt` — declares `<Feature>UiState` data class + `<Feature>Intent` sealed interface
- `<Feature>ViewModel.kt` — holds `StateFlow<UiState>`, exposes single `onIntent(intent)` entry point
- `<Feature>Screen.kt` — stateful composable collecting state; `<Feature>ScreenContent` stateless composable for layout

---

## Navigation

Three screens, state-based with no navigation library:

```
AppScreen.Mixer  (root)
    │  onStartSession()
    ▼
AppScreen.FocusSession
    │  onStartSession(config: SessionConfig)
    ▼
AppScreen.ActiveSession(config, sessionId)
    │  onFinish()
    ▼
AppScreen.Mixer  (back to root)
```

`FocusSession` also has a Close button → back to Mixer.  
There is no system back gesture handling; all navigation is user-initiated.  
`AnimatedContent` in `App.kt` drives all transitions with per-pair animation specs.

### Transition Specs

| Pair | Enter | Exit | Feel |
|------|-------|------|------|
| Mixer → FocusSession | `slideInVertically(420ms EaseOutCubic) { h/6 }` + `fadeIn(320ms)` | `fadeOut(220ms)` | Panel rises |
| FocusSession → Mixer | `fadeIn(320ms)` | `slideOutVertically(380ms EaseInCubic) { h/6 }` + `fadeOut(260ms)` | Panel drops |
| FocusSession → ActiveSession | `scaleIn(550ms EaseOutCubic, 0.94f)` + `fadeIn(450ms)` | `scaleOut(400ms, 1.04f)` + `fadeOut(300ms)` | Dive in |
| ActiveSession → Mixer | `scaleIn(550ms EaseOutCubic, 1.03f)` + `fadeIn(500ms)` | `fadeOut(400ms)` | Surface/breath out |

---

## Screen 1 — Mixer (Home)

**Files:** `feature/mixer/MixerScreen.kt`, `MixerViewModel.kt`, `MixerContract.kt`

### What it does

The immersive home screen. Users build their ambient soundscape, toggle sounds, adjust individual volumes, and control the organic motion engine. The play state is always visible via a floating bottom panel.

### Layout (top → bottom)

1. Full-screen atmospheric background (`ImmersiveBackground.kt`) — dark forest photo + gradient
2. Info icon (top-right) → opens `AboutSheet` (credits/sound licenses)
3. **Hero Session Button** (`HeroSessionButton.kt`) — large glassmorphic circle, 3 breathing rings, tapping navigates to FocusSession
4. **Category pill row** (`CategoryPillRow.kt`) — filter pills (All / Nature / Urban / etc.)
5. **Sound tiles list** (`SoundTile.kt`) — `LazyColumn` with section headers; per sound: icon, name, toggle, volume slider, organic motion toggle
6. **Current Mix Panel** (`CurrentMixPanel.kt`) — floating bottom bar: active sound count, play/pause; tapping opens `CurrentMixModal`

### Sound Catalog (9 sounds)

Rain (default on, 70%), Wind (default on, 50%), Thunder, Forest, Stream, Cafe, Fireplace, Brown Noise, Waves.

Each sound has: id, name, icon, audio file path, enabled state, volume (0.0–1.0), organicMotionEnabled.

### Current Mix Modal (`CurrentMixModal.kt`)

Full-screen overlay opened from the Current Mix Panel. Contains:
- Global organic motion toggle
- Per-sound: volume slider, organic motion toggle, remove button
- Global play/pause
- Save Mix button (opens `SaveMixDialog`)
- Done button

### Save Mix Dialog (`SaveMixDialog.kt`)

Dialog for naming and saving the current sound configuration as a named preset.

### Presets Sheet (`PresetsSheet.kt`)

Bottom sheet for browsing and loading saved mix presets.

### Mixer Use Cases (domain layer)

| Use Case | Action |
|----------|--------|
| `TogglePlaybackUseCase` | Play/pause all sounds |
| `ToggleSoundUseCase` | Enable/disable one sound |
| `AdjustVolumeUseCase` | Change one sound's volume |
| `ToggleOrganicMotionUseCase` | Toggle per-sound organic motion |
| `ToggleGlobalOrganicMotionUseCase` | Toggle organic motion for all active sounds |
| `RemoveFromMixUseCase` | Remove a sound from the active mix |
| `SelectCategoryUseCase` | Filter sound list by category |

### Data Layer

- `MixPresetRepository.kt` — persists named mixes (JSON via `JsonStore`)
- `AmbientStateRepository.kt` — persists ambient playback state
- `MixAudioOrchestrator.kt` — bridges ViewModel intent to `SoundMixer` audio engine

---

## Screen 2 — Focus Session Setup

**Files:** `feature/session/FocusSessionScreen.kt`, `FocusSessionViewModel.kt`, `FocusSessionContract.kt`

### What it does

Configuration screen before starting a timer. Two modes: **Focus** (Pomodoro) or **Sleep**.

### Focus Mode

- 3 built-in presets: 25/5 min × 4, 50/10 min × 4, 90 min deep focus × 1
- Custom card (expandable): steppers for focus minutes, break minutes, session count
- **Protect Focus card** (iOS only): toggle Screen Time app blocking; tapping opens a native FamilyActivityPicker via `ProtectFocusController` (expect/actual bridge)

### Sleep Mode

- Sleep duration stepper (default 45 min)
- Fade-out duration stepper (default 10 min)

### Output

Tapping "START SESSION" resolves a `SessionConfig` data class and navigates to ActiveSession.

### Protect Focus State Machine

`Idle → SheetOpen → SettingUp → SetupCompleted | Cancelled | PermissionDenied`

iOS implementation: `ScreenTimeManager.swift` (Swift) ← `ProtectFocusController.ios.kt` (actual).

---

## Screen 3 — Active Session (Timer)

**Files:** `feature/timer/ActiveSessionScreen.kt`, `ActiveSessionViewModel.kt`, `ActiveSessionContract.kt`

Sub-composables: `AtmosphericField.kt`, `SessionBackground.kt`, `SessionControls.kt`

### What it does

Running timer screen. Supports Focus mode (Pomodoro cycles with breaks) and Sleep mode (single countdown + audio fade).

### Focus Mode Flow

`Focus phase → Break phase → Focus phase → … (totalCycles) → Complete`

Auto-exits 2 seconds after completion.

### Sleep Mode Flow

Single countdown → fade-out period (sounds gradually dim to silence, black overlay fades in) → Complete ("REST" label). Exit button is the only control shown.

### Layout (bottom → top, visual layers)

1. `SessionBackground` — forest image + gradient overlay, phase-aware coloring
2. `AmbientBackgroundPulse` — slow radial glow breathing in sync with phase
3. Sleep fade-to-black overlay (0 → 0.6 alpha over fade-out duration)
4. `SessionTopBar` — close (X) button (hidden during sleep fade-out)
5. Phase label — "FOCUS SESSION" / "BREAK" / "SLEEP SESSION" / "REST" / "COMPLETE"
6. `AtmosphericField` — glassmorphic breathing circle (dual-frequency organic animation), contains:
   - Countdown timer text (64sp, ExtraLight, tabular nums)
   - Pause/Resume button (Focus mode only)
7. `ProgressSection` — "Cycle X of Y" + animated dot indicators (Focus mode only)
8. `BottomControls` — Skip phase + Stop buttons (hidden in sleep mode, hidden when complete)
9. `SleepExitButton` — single stop button (sleep mode only)

### Audio Integration

- Focus phase active: master volume → 1.0 (animated)
- Break / Paused / Completed: master volume → 0.0 (animated)
- On dispose: sends `null` to signal release audio resources
- Exit flow: `isExiting = true` → volume animates to 0 → 450ms delay → `onFinish()` called

### AtmosphericField Animation System

Dual-frequency organic breathing using `rememberInfiniteTransition`:

- **Primary breath** (70% weight): `FastOutSlowInEasing`, 4800ms half-cycle (Focus)
- **Secondary breath** (30% weight): `OrganicEasing (0.3, 0.0, 0.15, 1.0)`, 7200ms half-cycle (Focus)
- Combined: `breath = primary * 0.7 + secondary * 0.3` — never-repeating waveform
- Intensity multiplier: Focus=1.0, Break=0.5, Paused=0.3 (2000ms animated transition)
- Inner light drift: two independent `driftX`/`driftY` floats at different frequencies, creating a slowly wandering light path

### SessionBackground Phase Colors

Focus phase: deep blue-tinted. Break phase: warmer tones. Animated on phase change.

---

## Core Systems

### Audio Engine (`core/audio/`)

| File | Role |
|------|------|
| `SoundMixer.kt` | Manages all `AudioPlayer` instances; syncs playback to UI state |
| `AudioPlayer.kt` | `expect/actual` — platform wrapper for looped audio playback |
| `AudioPlayerFactory.kt` | Factory for creating `AudioPlayer` instances |
| `AudioPlayerHandle.kt` | Handle for controlling a playing sound |
| `OrganicMotionEngine.kt` | Continuously modulates per-sound volumes with smooth random drift |
| `AudioSettingsRepository.kt` | Persists audio settings |
| `AudioCommand.kt` | Sealed commands sent to the audio engine |

Audio files are `.m4a`, loaded from `composeResources/files/`.

### Organic Motion Engine

Runs a coroutine loop that slowly randomizes each active sound's effective volume within a range, creating a "living" soundscape that feels natural. Can be toggled globally or per-sound.

### Haptic System (`core/haptic/`)

- `HapticEngine.kt` — `expect/actual` per-platform implementation
- `HapticController.kt` — high-level coordinator
- `HapticFeedbackType.kt` — sealed types (Light, Medium, Heavy, Selection, etc.)
- `HapticSettingsRepository.kt` — user preference: haptics on/off

### Design System (`core/designsystem/`)

**Theme:** Dark-only. Creative direction: "The Digital Sanctuary" — dimly lit, atmospheric, premium, minimal.

Key rules:
- **Never use `Color.White` / `#ffffff`** — use `onSurface (#e0e6f1)` for primary text
- **Never use raw `Color(0xFF...)` literals** — always go through `MaterialTheme.colorScheme.*`
- **No ripple** — all interactive elements use `indication = null`
- **No `elevation`** — depth is achieved via the surface color ramp, not shadows (except `3.dp` and `8.dp` on floating elements)
- Press feedback: `scale(0.97f)` animated, not ripple

**Surface ramp (dark → light):**
`surface (#0c0e11)` → `surfaceContainer (#161a1f)` → `surfaceContainerHigh (#1b2027)` → `surfaceContainerHighest (#20262e)` → `surfaceBright (#2a3240)`

**Primary:** `#b7c8db` (cool blue-grey)

**Typography:** `FontFamily.Default` (Manrope planned). Key sizes: 64sp timer (ExtraLight), 11sp phase labels (letter-spaced), 15sp body (Light 300). Bold (700) is never used.

---

## iOS Native Integrations

### Live Activity (`iosMain/core/liveactivity/`)

Three distinct states shown on lock screen and Dynamic Island:

| State | Trigger |
|-------|---------|
| `AmbientPlayback` | Mixer playing, no session active |
| `FocusActive` | Focus timer running |
| `SleepActive` | Sleep timer running |

**Bridge architecture:**

```
KMP (iosMain)
  LiveActivityState (sealed) → LiveActivityController.push(state)
    → LiveActivityBridge (singleton handler protocol)
        → LiveActivityManager.swift (Swift, ActivityKit)
            → FocusRitualLiveActivity (Widget Extension, SwiftUI)
```

Widget Extension files: `FocusRitualAttributes.swift` (shared model, in both targets), `FocusRitualLiveActivity.swift` (dispatch), three view files (`AmbientPlaybackView`, `FocusSessionActiveView`, `SleepSessionActiveView`), `RitualTokens.swift` (design tokens), `LiveActivityButton.swift` (shared action button).

Live Activity lifecycle rules: only one FocusRitual Live Activity at a time; new start ends existing; stop/end from widget intents handled in extension before notifying main app; best-effort cleanup on app terminate.

### Protect Focus — Screen Time (`core/protectfocus/`)

- `ProtectFocusController.kt` — `expect/actual`; iOS actual bridges to `ScreenTimeManager.swift`
- iOS uses `FamilyControls` framework; presents `FamilyActivityPicker`
- Requests authorization; blocked app set persists across sessions
- Only available on iOS; Android actual is a no-op stub

---

## Data Persistence

| Data | Mechanism | Location |
|------|-----------|----------|
| Named mix presets | `MixPresetRepository` (JSON via `JsonStore`) | commonMain |
| Ambient playback state | `AmbientStateRepository` | commonMain |
| Audio settings | `AudioSettingsRepository` | commonMain |
| Haptic settings | `HapticSettingsRepository` | commonMain |
| Session preferences | `SessionPreferences` | feature/session |
| Protect Focus app selection | `ScreenTimeManager` (iOS system) | Swift |

---

## iOS Project Setup Requirements

- iOS deployment target: **iOS 18+** (required for ActivityKit, AppIntents)
- Widget Extension target: `FocusRitualWidget` (separate Xcode target)
- `FocusRitualAttributes.swift` added to **both** main app and widget extension targets
- Main app `Info.plist`: `NSSupportsLiveActivities = true`
- App Group capability on both targets: `group.com.focusritual.app`
- `FamilyControls` capability on main app target

---

## Build & Validation Commands

```bash
# Android compile (fast validation)
./gradlew :composeApp:compileDebugKotlinAndroid

# iOS KMP framework link
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Full Android debug build
./gradlew :composeApp:assembleDebug

# Run tests
./gradlew :composeApp:allTests

# Open Xcode (for full iOS build / Live Activity / ScreenTime testing)
open iosApp/iosApp.xcodeproj
```

> Live Activity, ScreenTime, and Widget changes require manual testing on a real device or simulator running iOS 18+.

---

## Active Plans & In-Progress Work (`plans/`)

| Plan | Status |
|------|--------|
| `mixer-refactor.md` | Active refactor of mixer internals |
| `mixer-persistence.md` | Mix preset persistence |
| `current-mix-modal-redesign.md` | CurrentMixModal UI redesign |
| `save-mix-dialog-redesign.md` | SaveMixDialog redesign |
| `save-mix-screen.md` | Save Mix as dedicated screen |
| `mixer-orb-animation.md` | Mixer orb animation work |
| `focus-session-sheet-readability.md` | Focus Session screen readability improvements |
| `haptic-feedback.md` | Haptic feedback implementation |
| `structure-refactor.md` | Code structure refactor |

---

## Key Conventions for AI Agents

- **MVI is mandatory** — every screen has `UiState` + `Intent` + `ViewModel.onIntent()`
- **Stateful/stateless composable split** — `<X>Screen` (collects state) + `<X>ScreenContent` (pure layout)
- **All UI in commonMain** — no platform-specific UI code
- **Platform APIs via expect/actual** — audio, haptic, protect focus, live activity bridge
- **Design system tokens only** — no raw color literals, no `Color.White`, no ripple
- **iOS bridge changes require both sides** — KMP `iosMain` actual + Swift `iosApp/` handler
- **Live Activity changes require both sides** — `core/liveactivity/` (KMP) + `FocusRitualWidget/` (Swift)
- **Trust Gradle over IDE** — KMP false positives are common in the IDE
- **No commits without user approval**
