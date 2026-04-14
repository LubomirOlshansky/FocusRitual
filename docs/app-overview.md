# FocusRitual — App Overview

## Idea

FocusRitual is an ambient focus and sleep companion app. It combines a **sound mixer** with **Pomodoro-style focus timers** and a **sleep mode** to help users stay productive or drift off to sleep with immersive ambient soundscapes.

The core experience: open the app → you're immediately inside a calm atmosphere with sounds playing → tap one button to start a focus session or sleep timer → the app guides you through timed focus/break cycles while blocking distracting apps and showing progress on the iOS lock screen via Live Activities.

**Platforms:** Android & iOS (Kotlin Multiplatform + Compose Multiplatform)

---

## Navigation Flow

```
┌─────────────┐      Start Session      ┌────────────────────┐     Start     ┌──────────────────────┐
│             │ ───────────────────────► │                    │ ────────────► │                      │
│   Mixer     │                          │   Focus Session    │               │   Active Session     │
│  (Home)     │ ◄─────────────────────── │   Setup            │               │   (Timer)            │
│             │        Close             │                    │               │                      │
└─────────────┘                          └────────────────────┘               └──────────┬───────────┘
       ▲                                                                                 │
       │                              Finish / Stop                                      │
       └─────────────────────────────────────────────────────────────────────────────────┘
```

Screen transitions use `Crossfade` with a 300ms `tween` animation.

---

## Screen 1: Mixer (Home Screen)

**File:** `feature/mixer/MixerScreen.kt`
**ViewModel:** `MixerViewModel` — manages sound playback state, organic motion engine
**State:** `MixerUiState`

### Purpose
The immersive home screen where users build their ambient soundscape by toggling and mixing sounds.

### Layout (top to bottom)

1. **Immersive Background** — full-screen atmospheric gradient backdrop
2. **About Button** (top-right) — info icon, opens the About Sheet
3. **Hero Session Button** (center-top) — large animated play button with breathing rings and glow; tapping it navigates to the Focus Session Setup screen
4. **Sound Tiles** — scrollable list of 9 ambient sounds, each with:
   - Toggle on/off
   - Volume slider
   - Organic Motion toggle (per-sound)
5. **Floating Current Mix Panel** (bottom) — shows active sound count + summary; tapping opens the Current Mix Modal; includes a global play/pause button

### Available Sounds
| Sound | Default State |
|-------|--------------|
| Rain | Enabled, 70% volume |
| Thunder | Off |
| Wind | Enabled, 50% volume |
| Forest | Off |
| Stream | Off |
| Cafe | Off |
| Fireplace | Off |
| Brown Noise | Off |
| Waves | Off |

### User Actions (MixerIntent)
- **TogglePlayback** — play/pause the entire mix
- **ToggleSound(soundId)** — enable/disable a specific sound
- **AdjustVolume(soundId, volume)** — change individual sound volume (0.0–1.0)
- **ToggleOrganicMotion(soundId)** — enable/disable per-sound organic motion
- **ToggleGlobalOrganicMotion** — toggle organic motion for all active sounds at once
- **RemoveFromMix(soundId)** — remove a sound from the active mix

### Modals & Sheets
- **About Sheet** (`AboutSheet.kt`) — app info + sound credits list (title, author, license, source for each sound)
- **Current Mix Modal** (`CurrentMixModal.kt`) — full-screen overlay for fine-tuning the active mix:
  - Global organic motion toggle
  - Per-sound volume sliders
  - Per-sound organic motion toggles
  - Remove sound from mix
  - Play/pause all
  - Done button to dismiss

### Audio Engine (core/audio)
- **SoundMixer** — manages audio player instances, caches audio data, syncs playback state to match UI
- **AudioPlayer** — platform `expect/actual` wrapper for playing looped audio (Android/iOS)
- **OrganicMotionEngine** — continuously modulates individual sound volumes with smooth random drift to create a living, breathing soundscape
- **SoundResources** — maps sound IDs to audio file paths (`files/*.m4a`)

---

## Screen 2: Focus Session Setup

**File:** `feature/session/FocusSessionScreen.kt`
**ViewModel:** `FocusSessionViewModel`
**State:** `FocusSessionUiState`

### Purpose
Configure a focus session (Pomodoro) or sleep session before starting the timer.

### Layout (top to bottom)

1. **Header** — title ("FOCUS SESSION" or "SLEEP SESSION") + close (X) button
2. **Session Mode Toggle** — switch between **Focus** and **Sleep** modes (animated crossfade)

#### Focus Mode Content
3. **Preset Radio Buttons** — 3 built-in presets:
   - 25 min focus / 5 min break (4 cycles)
   - 50 min focus / 10 min break (4 cycles)
   - 90 min deep focus (1 cycle, no break)
4. **Custom Card** — expandable card for custom timer values:
   - Focus duration stepper (minutes)
   - Break duration stepper (minutes)
   - Number of sessions stepper
5. **Protect Focus Card** (iOS only) — Screen Time integration:
   - Shows blocked app count when configured
   - Toggle to enable/disable app blocking
   - Tap to open Protect Focus Setup Sheet → choose apps to block via iOS FamilyControls picker

#### Sleep Mode Content
3. **Sleep Configuration Card**:
   - Sleep duration stepper (default: 45 min)
   - Fade-out duration stepper (default: 10 min)

#### Footer
6. **"START SESSION" Button** — primary CTA, starts the timer with resolved config

### User Actions (FocusSessionIntent)
- **SelectPreset(presetId)** — pick a built-in preset
- **SelectCustom** — switch to custom timing
- **AdjustFocus(delta)** — increment/decrement focus minutes
- **AdjustBreak(delta)** — increment/decrement break minutes
- **AdjustSessions(delta)** — increment/decrement cycle count
- **AdjustSleepDuration(delta)** — increment/decrement sleep timer duration
- **AdjustSleepFadeOut(delta)** — increment/decrement fade-out duration
- **StartSession** — resolve config and navigate to Active Session
- **Close** — return to Mixer

### Protect Focus (iOS — Screen Time API)
- Uses `ScreenTimeManager` (Swift) via `ProtectFocusController` (KMP bridge)
- Requests Family Controls authorization
- Presents the iOS app picker to choose apps to block during sessions
- States: Idle → SheetOpen → SettingUp → SetupCompleted / Cancelled / PermissionDenied

---

## Screen 3: Active Session (Timer)

**File:** `feature/timer/ActiveSessionScreen.kt`
**ViewModel:** `ActiveSessionViewModel`
**State:** `ActiveSessionUiState`

### Purpose
The running timer screen — displays countdown, manages focus/break phases, and provides session controls. Supports both Focus (Pomodoro) and Sleep modes.

### Layout (top to bottom)

1. **Timer Background** (`SessionBackground.kt`) — full-screen forest image with gradient overlay, phase-aware coloring
2. **Ambient Background Pulse** — subtle radial glow that breathes in and out
3. **Sleep Fade-to-Black Overlay** — black overlay that fades in during sleep fade-out
4. **Session Top Bar** — close (X) button (hidden during sleep fade-out)
5. **Phase Label** — "FOCUS SESSION", "BREAK", "SLEEP SESSION", "REST", or "COMPLETE"
6. **Atmospheric Field** (`AtmosphericField.kt`) — glassmorphic breathing circle animation containing:
   - **Countdown Timer** — large 64sp display (e.g., "24:59")
   - **Pause/Play Button** — 64dp circle (Focus mode only, hidden during sleep)
7. **Progress Section** (`SessionControls.kt`) — "Cycle X of Y" + animated dot indicators (Focus mode only)
8. **Bottom Controls** (`SessionControls.kt`):
   - Focus mode: Skip phase + Stop buttons
   - Sleep mode: Exit button only

### Session Phases (SessionPhase)
- **Focus** — countdown running, sounds at full volume
- **Break** — rest period between focus cycles, sounds dimmed

### Timer Lifecycle
1. **Focus mode**: Focus → Break → Focus → Break → ... (repeats for `totalCycles`) → Complete
2. **Sleep mode**: Single countdown → fade-out period (sounds gradually dim to silence) → Complete ("REST" label)

### Sound Integration
- During Focus phase: master volume animates to 1.0
- During Break/Paused/Completed: master volume animates to 0.0
- On dispose: sends `null` to release audio resources
- Sleep fade-out: dedicated `SLEEP_FADE_OUT_MS` constant controls gradual audio dimming

### User Actions (ActiveSessionIntent)
- **TogglePause** — pause/resume the timer (Focus mode only)
- **Skip** — skip current phase (Focus mode only)
- **Stop** — end the session and return to Mixer

### Auto-behaviors
- Auto-exits 2 seconds after session completion
- Exit animation: 450ms transition before navigating back to Mixer

---

## iOS Live Activity (Lock Screen + Dynamic Island)

**Architecture:** KMP → Swift bridge → ActivityKit → Widget Extension

### Three Live Activity Modes

| Mode | Trigger | Visual Style |
|------|---------|-------------|
| **Ambient Playback** | Mix playing, no session | Calm, minimal — shows mix summary + sound count |
| **Focus Session** | Focus timer running | Structured — progress ring, timer countdown, phase, cycle info |
| **Sleep Session** | Sleep timer running | Soft, gentle — countdown, fade-out progress |

### Components
- **FocusRitualAttributes** (Swift) — static `sessionType` ("ambient"/"focus"/"sleep") + dynamic `ContentState` with all timer/mix fields
- **FocusRitualLiveActivity** — Widget dispatching to 3 views based on session type
- **Views:**
  - `AmbientPlaybackView` — minimal playback indicator
  - `FocusSessionActiveView` — timer + progress ring + phase/cycle info
  - `SleepSessionActiveView` — sleep timer + fade-out info
- **LiveActivityButton** — interactive button using `LiveActivityIntent` (AppIntents framework)
- **Dynamic Island** — compact leading/trailing + expanded regions for each mode

### Live Activity Actions (from Lock Screen)
- Toggle pause/resume
- Stop mix
- Skip phase
- End session

### Bridge (KMP ↔ Swift)
- `LiveActivityEffect` (commonMain) — Composable that syncs mixer + session state to Live Activity
- `LiveActivityHandler` (protocol) — implemented by `LiveActivityManager` in Swift
- Passes: isPaused, mixSummary, activeSoundCount, remainingSeconds, totalSeconds, phase, currentCycle, totalCycles, fadeOutMinutes

---

## Design System

**Package:** `core/designsystem`

### Theme
- **Material 3** dark theme
- Custom color palette (`Color.kt`), typography (`Type.kt`), and theme wiring (`Theme.kt`)

### Reusable Components
| Component | Purpose |
|-----------|---------|
| `PlayButton` | 96dp animated glass circle with breathing rings + glow |
| `SoundTile` | Sound card with icon, toggle, volume slider, organic motion |
| `VolumeSlider` | Custom-styled volume control |
| `AirPlayButton` | iOS AirPlay output picker |
| `ProtectFocusCard` | Card showing Screen Time blocking status + toggle |
| `ProtectFocusSetupSheet` | Bottom sheet for configuring app blocking |

---

## Technical Architecture

```
commonMain/
├── App.kt                          — Navigation + screen composition
├── Platform.kt                     — expect declarations
├── core/
│   ├── audio/
│   │   ├── AudioPlayer.kt          — expect/actual audio playback
│   │   ├── SoundMixer.kt           — Multi-track mixer engine
│   │   ├── OrganicMotionEngine.kt  — Volume drift algorithm
│   │   └── SoundResources.kt       — Sound ID → file path mapping
│   ├── designsystem/
│   │   ├── component/               — Shared UI components
│   │   └── theme/                   — Material 3 theme
│   ├── liveactivity/
│   │   └── LiveActivityEffect.kt   — KMP → iOS Live Activity sync
│   └── protectfocus/
│       ├── ProtectFocusContract.kt  — State machine for Screen Time setup
│       └── ProtectFocusController.kt — Platform bridge
├── feature/
│   ├── about/                       — About sheet + sound credits
│   ├── mixer/                       — Home/mixer screen (MVVM)
│   ├── session/                     — Session setup screen (MVVM)
│   └── timer/                       — Active session screen (MVVM)

androidMain/                         — Android Platform, AudioPlayer, MainActivity
iosMain/                             — iOS Platform, AudioPlayer, Live Activity bridge

iosApp/                              — Swift wrapper
├── LiveActivityManager.swift        — ActivityKit integration
├── ScreenTimeManager.swift          — FamilyControls / Screen Time
├── LiveActivityActionObserver.swift — Handle LA button taps
└── FocusRitualWidget/               — Widget Extension (Live Activity views)
```

### Patterns
- **MVVM** with sealed `Intent` interfaces (unidirectional data flow)
- **expect/actual** for platform APIs (audio, Screen Time, Live Activities)
- **Compose Multiplatform** for 100% shared UI
- **StateFlow** + `collectAsStateWithLifecycle` for state observation
- **Crossfade** navigation (no Jetpack Navigation library)
