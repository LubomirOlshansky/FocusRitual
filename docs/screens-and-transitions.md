# FocusRitual — Screens & Navigation Transitions

**Purpose:** Reference document for AI agents. Describes every screen's content and the
animated transitions between them. All code lives in `composeApp/src/commonMain/kotlin/com/focusritual/app/`.

---

## Screen Overview

The app has **3 screens** controlled by a `sealed interface AppScreen` in `App.kt`.
Navigation is state-based: `var currentScreen: AppScreen` drives an `AnimatedContent` composable
with per-transition animation specs. There is no navigation library.

```
AppScreen.Mixer  (root, always the start destination)
    │
    ▼  onStartSession()
AppScreen.FocusSession
    │
    ▼  onStartSession(config: SessionConfig)
AppScreen.ActiveSession(config, sessionId)
    │
    ▼  onFinish()  ──────────────────────────────────┐
                                                      │
AppScreen.Mixer  ◄────────────────────────────────────┘
```

Back navigation: FocusSession has an explicit Close button → Mixer.
There is no system back handling; transitions are always user-initiated.

---

## Screen 1 — Mixer (`AppScreen.Mixer`)

**File:** `feature/mixer/MixerScreen.kt`  
**ViewModel:** `MixerViewModel` (hoisted at App level, survives screen transitions)

### Visual composition
- **Full-screen immersive background** — dark forest photo (`background.png`) with a gradient overlay.
  Composable: `ImmersiveBackground()`.
- **Hero play button** — 96 dp glassmorphic circle, centered near top (120 dp top padding).
  Three breathing rings animate in sync. Composable: `HeroSessionButton`. Tapping it triggers
  `onStartSession()` → navigates to FocusSession.
- **Info button** — small `ⓘ` icon top-right (62 dp from top, 24 dp from right edge), opens
  `AboutSheet` (bottom sheet).
- **Category pill row** — horizontal filter pills (All / Nature / Urban / etc.) below the hero.
  Composable: `CategoryPillRow`.
- **Sound tiles list** — `LazyColumn` of `SoundTile` composables grouped by category with
  `SectionHeader` dividers. Each tile has: icon, name, toggle, volume slider (independent),
  organic-motion toggle. Active tiles use `surfaceContainerHigh` background; inactive use
  `surfaceContainer`.
- **Floating Current Mix Panel** — pinned at the bottom center, shows active sound count +
  master play/pause. Tapping it opens `CurrentMixModal` (full modal overlay with master volume
  and per-sound controls).

### State
- `isPlaying: Boolean` — master play/pause
- `sounds: List<SoundState>` — per-sound enabled/volume/organic-motion state
- `selectedCategory: SoundCategory` — pill filter

---

## Screen 2 — Focus Session Config (`AppScreen.FocusSession`)

**File:** `feature/session/FocusSessionScreen.kt`  
**ViewModel:** `FocusSessionViewModel` (created fresh on entry, not hoisted)

### Visual composition
- **Dark solid background** — `MaterialTheme.colorScheme.surface` (no image).
- **Header row** — "FOCUS SESSION" or "SLEEP SESSION" label (14 sp, 2 sp letter spacing) +
  Close `✕` icon button top-right. Tapping Close navigates back to Mixer.
- **Mode toggle** — two-tab switcher: Focus / Sleep. Composable: `SessionModeToggle`.
  Inner content crossfades (350 ms tween) between the two modes.
- **Focus mode content** (scrollable column):
  - Preset radio rows (`PresetRow`) — e.g. "25 / 5 min", "50 / 10 min".
  - Expandable custom card (`CustomCard`) — steppers for focus minutes, break minutes, sessions.
  - Protect Focus card (`ProtectFocusCard`) — expect/actual; iOS shows Screen Time shield entry.
    Tapping opens `ProtectFocusSetupSheet` bottom sheet (also expect/actual; iOS shows native
    FamilyActivityPicker).
- **Sleep mode content** (scrollable column):
  - `SleepConfigurationCard` — steppers for total sleep duration + fade-out duration.
- **Footer CTA** — "START SESSION" full-width button (`primary` color, 12 dp corners, 16 dp
  padding). Tapping resolves `SessionConfig` and navigates to ActiveSession.

### State
- `selectedPresetId`, `customFocusMinutes`, `customBreakMinutes`, `customSessions` — config choices
- `protectFocusState` — local state machine (Idle → SheetOpen → SettingUp → completed)

---

## Screen 3 — Active Session (`AppScreen.ActiveSession`)

**File:** `feature/timer/ActiveSessionScreen.kt`  
**ViewModel:** `ActiveSessionViewModel` (keyed by `sessionId`, hoisted at App level for Live Activity)

### Visual composition
- **Dynamic background** — `TimerBackground` composable; color shifts between Focus phase
  (deep blue-tinted) and Break phase (warmer), animated on phase change.
- **Ambient background pulse** — `AmbientBackgroundPulse`; slow radial glow that breathes in
  sync with the phase.
- **Top bar** — `SessionTopBar` with a ✕ close button (triggers Stop intent → `isExiting` flow).
  Hidden during sleep fade-out.
- **Phase label** — small caps label ("FOCUS", "BREAK", etc.), 11 sp, 2 sp letter spacing,
  `onSurfaceVariant` color.
- **Breathing circle** — `AtmosphericField` composable centered on screen. Contains:
  - Countdown timer text (64 sp ExtraLight, tabular numbers). Hidden in sleep mode during fade.
  - Pause / Resume icon button (64 dp circle, `primaryContainer` tinted).
- **Progress dots** — `ProgressSection` at the bottom: one dot per cycle, filled dot = completed.
  Hidden in sleep mode.
- **Bottom controls** — `BottomControls`: Skip phase button + Stop button. Hidden when session
  is completed or in sleep mode.
- **Sleep exit button** — `SleepExitButton`: single stop button shown only in sleep mode.
- **Sleep fade-to-black overlay** — semi-transparent black `Box` that ramps from 0 → 0.6 alpha
  over the configured fade-out duration when `isSleepFadingOut = true`.

### Exit flow
1. User taps Stop (or session completes): `isExiting = true`.
2. `animatedVolume` ramps to 0 f over 400 ms (audio fade-out).
3. `delay(450)` then `onFinish()` is called → outer `AnimatedContent` fires the
   ActiveSession → Mixer transition.

### State
- `phase: SessionPhase` — Focus / Break
- `remainingFormatted: String` — countdown display ("24:37")
- `isPaused`, `isCompleted`, `isSleepMode`, `isSleepFadingOut`
- `currentCycle`, `totalCycles`

---

## Transitions (`App.kt` — `AnimatedContent` block)

All transitions use `SizeTransform(clip = false)` so content is never cropped during scale
animations. Scale origin is `Alignment.Center`.

### Mixer → FocusSession
**Feel:** "Lifting a panel" — the config sheet rises up over the ambient mixer.

| | Spec |
|---|---|
| **Entering (FocusSession)** | `slideInVertically(tween(420, EaseOutCubic)) { height / 6 }` + `fadeIn(tween(320))` |
| **Exiting (Mixer)** | `fadeOut(tween(220))` |

FocusSession slides in from ~1/6 of the screen height below, easing out with a cubic curve.
Mixer fades out quickly (220 ms) so the incoming screen dominates.

---

### FocusSession → Mixer (close / back)
**Feel:** Symmetric dismissal — the config panel drops back down.

| | Spec |
|---|---|
| **Entering (Mixer)** | `fadeIn(tween(320))` |
| **Exiting (FocusSession)** | `slideOutVertically(tween(380, EaseInCubic)) { height / 6 }` + `fadeOut(tween(260))` |

FocusSession accelerates downward with EaseInCubic (starts slow, ends fast — natural gravity feel).
Mixer fades back in gently.

---

### FocusSession → ActiveSession
**Feel:** "Diving in" / immersion start — the ritual begins cinematically.

| | Spec |
|---|---|
| **Entering (ActiveSession)** | `scaleIn(tween(550, EaseOutCubic), initialScale = 0.94f)` + `fadeIn(tween(450))` |
| **Exiting (FocusSession)** | `scaleOut(tween(400), targetScale = 1.04f)` + `fadeOut(tween(300))` |

ActiveSession starts slightly small (0.94 ×) and grows to full size — slow and deliberate (550 ms).
FocusSession scales slightly past full size (1.04 ×) as it fades, creating a depth-through effect.
This is the longest transition in the app — intentional premium pacing.

---

### ActiveSession → Mixer
**Feel:** "Surfacing" / breath out — gentle return to the ambient world after the session.

| | Spec |
|---|---|
| **Entering (Mixer)** | `scaleIn(tween(550, EaseOutCubic), initialScale = 1.03f)` + `fadeIn(tween(500))` |
| **Exiting (ActiveSession)** | `fadeOut(tween(400))` |

Mixer arrives slightly zoomed in (1.03 ×) and eases back to 1.0 — a slow breath-in. The session
screen simply fades out. The combined 500 ms fade-in overlaps with the 450 ms audio fade-out that
happens internally in `ActiveSessionScreen` before `onFinish()` is even called, so audio and visual
fade finish at roughly the same time.

---

### Fallback (any other transition)
`fadeIn(tween(300)) togetherWith fadeOut(tween(300))` — matches the project's 300 ms default.

---

## Navigation Rules for AI

- `AppScreen.Mixer` is the **root** and **only** destination reachable from both other screens.
- `AppScreen.FocusSession` is a **temporary overlay** over Mixer (conceptually a sheet, but
  implemented as a full screen for layout simplicity).
- `AppScreen.ActiveSession` carries a `SessionConfig` data class + an incrementing `sessionId`
  int. The `sessionId` is used as the ViewModel key to force a fresh `ActiveSessionViewModel`
  on each session start.
- `MixerViewModel` is **never recreated** — it is hoisted at `App()` level and passed into
  `MixerScreen` as a parameter, ensuring audio playback survives navigation.
- `ActiveSessionViewModel` is **hoisted at `App()` level too** (keyed by sessionId) specifically
  so `LiveActivityEffect` can read session state from outside the active screen composable.
  This is unusual but intentional — do not move it inside `ActiveSessionScreen`.

---

## Files Quick Reference

| File | Purpose |
|---|---|
| `App.kt` | Root composable, `AnimatedContent`, ViewModel hoisting, `LiveActivityEffect` |
| `feature/mixer/MixerScreen.kt` | Mixer UI (background, hero button, sound tiles, mix panel) |
| `feature/mixer/MixerViewModel.kt` | Audio orchestration, `setSessionMasterVolume(Float?)` |
| `feature/session/FocusSessionScreen.kt` | Session config UI (presets, custom, sleep, Protect Focus) |
| `feature/session/FocusSessionViewModel.kt` | Config state, `resolveConfig()` |
| `feature/timer/ActiveSessionScreen.kt` | Timer UI (breathing circle, phase label, controls) |
| `feature/timer/ActiveSessionViewModel.kt` | Countdown timer, phase/cycle management |
| `core/designsystem/theme/` | Color tokens, typography, `FocusRitualTheme` |
| `core/audio/SoundMixer.kt` | Multi-player audio orchestrator |
