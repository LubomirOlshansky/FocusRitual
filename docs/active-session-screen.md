# Active Focus Session Screen — Full Architecture & Animation Documentation

## Overview

This is a Pomodoro-style focus timer screen in a **Kotlin Multiplatform (KMP) Compose Multiplatform** app. The screen shows a countdown timer inside a breathing glassmorphic circle animation, with session controls. It is split into **4 files** by single responsibility:

| File | Responsibility |
|---|---|
| `ActiveSessionScreen.kt` | Screen state management + layout composition |
| `AtmosphericField.kt` | Glassmorphic breathing circle animation |
| `SessionBackground.kt` | Background image + ambient pulse |
| `SessionControls.kt` | Top bar, progress indicators, bottom controls |

All files are in package `com.focusritual.app.feature.timer`.

---

## File 1: `ActiveSessionScreen.kt`

Contains two composables:

### `ActiveSessionScreen` (public, stateful) — the screen entry point

- Takes `SessionConfig`, `onFinish` callback, `onSoundControl: (Float?) -> Unit` callback, and a ViewModel
- Collects `ActiveSessionUiState` from `ActiveSessionViewModel.uiState` (StateFlow via `collectAsStateWithLifecycle`)
- Manages exit flow: sets `isExiting = true`, waits 450ms, then calls `onFinish()`
- Manages sound volume: animates volume 0→1 based on phase (Focus=1, Break/Paused/Completed=0), sends via `onSoundControl(animatedVolume)`. Sends `null` on dispose to signal "release audio"
- Auto-exits 2 seconds after session completion

### `ActiveSessionScreenContent` (private, stateless) — the layout

Layer order (bottom to top):

1. `TimerBackground` — full-screen forest image with gradient overlay
2. `AmbientBackgroundPulse` — subtle full-screen radial glow breathing
3. Main content Column with system bar padding:
   - `SessionTopBar` — close (X) button, top-left
   - Center area (weighted, fills available space):
     - Phase label ("FOCUS SESSION" / "BREAK") — 11sp, uppercase feel, 60% alpha `onSurfaceVariant`
     - 48dp spacer
     - `AtmosphericField` wrapping:
       - Timer text: 64sp, `ExtraLight`, negative letter spacing, `FontFamily.Default`, centered with `widthIn(min = 220.dp)` to prevent layout shift
       - 16dp spacer
       - Pause/Play button: 64dp circle, `primaryContainer` at 40% alpha bg, `primary` tint icon (32dp)
   - `ProgressSection` — "Cycle X of Y" text + animated dot bars
   - 24dp spacer
   - `BottomControls` — pill-shaped row with skip + stop buttons
   - 16dp spacer

### Key design decisions

- Timer text uses NO `Crossfade` or animation wrapper — rendered directly to prevent flickering
- `widthIn(min = 220.dp)` + `TextAlign.Center` ensures the timer doesn't shift layout when digits change
- `FontFamily.Default` prevents platform font issues
- Pause button uses `clickable` with `indication = null` (no ripple) for premium feel

---

## File 2: `AtmosphericField.kt` — The Breathing Circle Animation

This is the core visual feature. It creates a **glassmorphic circle** with an organic, multi-layered breathing animation that feels like a living atmospheric presence.

### Color constants

- `SurfaceContainer = Color(0xFF161A1F)` — dark surface for the circle fill
- `OutlineVariant = Color(0xFF424851)` — ghost border color
- `Primary = Color(0xFFB7C8DB)` — cool blue-grey for glows

### Easing constants

- `OrganicEasing = CubicBezierEasing(0.3, 0.0, 0.15, 1.0)` — softer, asymmetric easing for secondary breath
- `DriftEasing = CubicBezierEasing(0.4, 0.0, 0.6, 1.0)` — very gentle easing for inner light drift

### Parameters

- `phase: SessionPhase` — Focus or Break
- `isPaused: Boolean`
- `content: @Composable () -> Unit` — timer + pause button placed inside the circle

### Animation system — Dual-frequency organic breathing

Uses `rememberInfiniteTransition` with **two breath oscillators** at different frequencies, combined 70/30, to create a never-exactly-repeating organic rhythm.

#### Primary breath (70% weight)

- `FastOutSlowInEasing` — CSS `cubic-bezier(0.4, 0, 0.2, 1)`
- Half-cycle: Focus = 4800ms, Break = 5800ms, Paused = 6000ms
- `RepeatMode.Reverse`

#### Secondary breath (30% weight)

- `OrganicEasing` — softer, more asymmetric curve
- Half-cycle: Focus = 7200ms, Break = 9000ms, Paused = 8500ms
- `RepeatMode.Reverse`
- Because the two frequencies are not harmonically related, the combined waveform drifts over time, creating a living, non-mechanical feel

#### Combined breath

`breath = primaryBreath * 0.7f + secondaryBreath * 0.3f`

This produces a breathing pattern that is never perfectly symmetrical — sometimes the inhale lingers, sometimes the exhale comes sooner.

#### Intensity multiplier

Animates smoothly between states with **2000ms** transition:

- Focus: `1.0` (full animation, deep and alive)
- Break: `0.5` (half strength, quieter)
- Paused: `0.3` (barely moving, nearly still)

#### Inner light drift

Two independent `animateFloat` values (`driftX`, `driftY`) oscillate slowly at different rates:

- `driftX`: Focus = 11s, Break = 15s, Paused = 18s half-cycle
- `driftY`: Focus = 14s, Break = 19s, Paused = 22s half-cycle
- Both use `DriftEasing` for ultra-smooth motion
- Separate `driftIntensity` (2500ms transition): Focus = 1.0, Break = 0.4, Paused = 0.15
- The different X/Y frequencies create a slowly wandering path that never repeats

#### Derived values

- `b = breath * intensity` — main animation driver
- `outerScale = 1 + b * 0.1` — outer pulse scale
- `outerAlpha = (0.4 + b * 0.4) * intensity` — outer pulse opacity
- `circleScale = 1 + b * 0.05` — main circle + halo scale
- `glowAlpha = (0.06 + b * 0.04) * intensity` — glow halo opacity
- `shadowAlpha = (0.03 + (1-b) * 0.05) * intensity` — counter-shadow (inverse of breath)

### Visual layers (4 layers + 2 draw effects, bottom to top)

#### Layer 1: Outer breathing pulse — 480dp circle

- Scale: `outerScale` (grows up to 10%)
- Alpha: `outerAlpha` (fades in/out with breath)
- Fill: `Brush.radialGradient` with `Primary` at 10%→5%→1%→transparent
- Purpose: Large soft glow surrounding the entire circle

#### Layer 2: Counter-shadow ring — 370dp circle

- Scale: same as main circle (`circleScale`)
- Alpha: `shadowAlpha` — **inverse response**: darkens when breath contracts, softens when breath expands
- Fill: `Brush.radialGradient` — transparent center → 12% black at 75% → 5% black at 90% → transparent
- Purpose: Adds cinematic depth by creating subtle density contrast around the circle edge

#### Layer 3: Glow halo — 360dp circle

- Scale: `circleScale`
- Alpha: `glowAlpha`
- Fill: `Brush.radialGradient` with `Primary` at 8%→3%→transparent
- Purpose: Luminance ring simulating a soft box-shadow

#### Layer 4: Main glassmorphic circle — 320dp circle

- Scale: `circleScale` (grows up to 5%)
- Background: `SurfaceContainer` at 20% alpha
- **Clipped** to `CircleShape` (contains the draw effects)
- Border: 1dp `OutlineVariant` at 10% alpha (ghost border)
- Content: timer + pause button centered inside

##### Draw effect A: Inner counter-shadow (via `drawBehind`)

- `innerShadow = (1 - b) * 0.06` — denser center when breath contracts, lighter when expanded
- `Brush.radialGradient` from black at `innerShadow` alpha → 30% of that at 40% → transparent
- Creates subtle tonal depth pulsing inside the circle

##### Draw effect B: Inner light drift (via `drawBehind`)

- Center offset: `driftX * driftIntensity * 8%` width, `driftY * driftIntensity * 6%` height
- `lightAlpha = (0.04 + b * 0.03) * driftIntensity`
- `Brush.radialGradient` with `Primary` at `lightAlpha` → 40% of that at 0.4 → transparent
- Radius: 35% of circle width
- Purpose: Soft internal light mass that wanders slowly through the circle like light shifting through mist

### Why this works

- **Organic rhythm**: Two breath oscillators at non-harmonic frequencies create a pattern that never exactly repeats, avoiding the "mechanical metronome" feel
- **Counter-shadow depth**: The dark ring inverse-reacting to breath creates a cinematic push–pull between light and shadow
- **Inner life**: The drifting light mass gives the circle internal depth — it feels like something is alive inside fog, not a static UI element
- **Phase differentiation**: Focus mode has deep breathing + active drift; Break mode significantly reduces both, making the circle feel quieter and more still; transitions take 2–2.5 seconds for smooth state changes
- **Timer safety**: All animation runs via `graphicsLayer` (GPU compositing) and `drawBehind` (draw phase) — zero content recomposition, timer text stays perfectly crisp
- **Performance**: No blur kernels, no particles, no noise — just layered opacity, scale, and gradient offsets

---

## File 3: `SessionBackground.kt`

### `TimerBackground` — full-screen background

- Shows a forest image (`Res.drawable.background`) cropped to fill
- Overlays a vertical gradient from top to bottom: `Color(0xFF0c0e11)` (almost black)
- Gradient alpha is phase-aware (animated via `animateFloatAsState`, 1500ms):
  - Focus: `0.82` — forest slightly visible through
  - Paused: `0.90` — darker
  - Break: `0.92` — darkest
- The gradient goes: 70% alpha at top → full alpha at 30% → solid at bottom

### `AmbientBackgroundPulse` — full-screen subtle radial glow

- Uses `rememberInfiniteTransition` + `animateFloat` (0→1→0)
- `LinearOutSlowInEasing`
- Scale: `0.9 + pulse * 0.2` (oscillates between 0.9× and 1.1×)
- Duration: Focus = 7s, Break = 11s, Paused = 14s (per half-cycle)
- Alpha (via `animateFloatAsState`): Focus = 0.06, Break = 0.025, Paused = 0.015
- Fill: `Brush.radialGradient` centered, 700f radius
  - BlueGrey(0xFF2A3A4E) at 40% → MidnightBlue(0xFF1B2838) at 20% → transparent
- Purpose: Adds very subtle tonal depth movement to the background, almost imperceptible

---

## File 4: `SessionControls.kt`

### `SessionTopBar` — top bar with close button only

- Full-width Box, 24dp horizontal / 16dp vertical padding
- Single `IconButton` (36dp) aligned `CenterStart` (left)
- Close icon: `Icons.Default.Close`, 20dp, `onSurface` at 60% alpha
- No title text, no menu icon

### `ProgressSection` — cycle progress indicator

- Text: "Cycle X of Y" or "Session complete" — `bodyMedium`, `onSurfaceVariant`
- Row of dot bars: `totalCycles` bars, each 32dp × 2dp, rounded 1dp corners
- Color per bar (animated with `animateColorAsState`, 500ms tween):
  - Completed cycles: `primary` (full)
  - Current cycle: `primary` at 40% alpha
  - Future cycles: `surfaceContainerHighest`

### `BottomControls` — floating pill with skip + stop

- Centered pill container: `RoundedCornerShape(999.dp)`, `surfaceBright` at 60% alpha, 32dp horizontal / 16dp vertical padding
- Skip button: `IconButton` 40dp, `Icons.Default.SkipNext`, `onSurfaceVariant` tint
- Stop button: 48dp circle, `onSurface` at 15% alpha bg, `Icons.Default.StopCircle`, `onSurface` tint
- Both use no ripple indication

---

## State Model

### `SessionPhase`

Sealed interface with `Focus` and `Break` cases.

### `ActiveSessionUiState`

| Field | Type | Example |
|---|---|---|
| `remainingFormatted` | `String` | `"24:12"` |
| `phaseLabel` | `String` | `"FOCUS SESSION"` |
| `phase` | `SessionPhase` | `SessionPhase.Focus` |
| `isPaused` | `Boolean` | `false` |
| `isCompleted` | `Boolean` | `false` |
| `currentCycle` | `Int` | `2` |
| `totalCycles` | `Int` | `4` |

### `ActiveSessionIntent`

Sealed interface with: `TogglePause`, `Skip`, `Stop`

---

## Color Palette (from design system)

The screen uses Material 3 theme colors mapped to this dark palette:

| Token | Hex | Usage |
|---|---|---|
| `background / surface` | `#0C0E11` | Near-black base |
| `surfaceContainer` | `#161A1F` | Circle fill |
| `surfaceContainerHighest` | `#20262E` | Inactive dot bars |
| `surfaceBright` | `#252D36` | Bottom controls pill |
| `outlineVariant` | `#424851` | Ghost border |
| `primary` | `#B7C8DB` | Glow, active dots, pause icon |
| `primaryContainer` | `#384858` | Pause button bg |
| `onSurface` | `#E0E6F1` | Timer text, stop icon |
| `onSurfaceVariant` | `#A5ABB6` | Labels, skip icon |

**Design rule:** No bright white, no neon, no harsh borders. Ghost borders at 10% alpha max. All transitions minimum 300ms with soft easing.
