# Onboarding Feature — Implementation Overview

Kotlin Multiplatform / Compose Multiplatform. All UI in `commonMain`. Dark-only design system.

---

## File Map

```
feature/onboarding/
  OnboardingContract.kt          — MVI state/intent types
  OnboardingViewModel.kt         — business logic, audio fade, completion signal
  OnboardingScreen.kt            — stateful host (creates VM, listens for completion)
  OnboardingScreenContent.kt     — stateless, AnimatedContent step router
  data/
    OnboardingRepository.kt      — JsonStore-backed persistence (hasCompleted flag)
  components/
    AtmosphericBackdrop.kt       — stacks all background layers
    ForestKenBurnsLayer.kt       — slow scale+translateY on `background` drawable
    DriftingMistLayer.kt         — Canvas radial gradients, horizontal drift
    AmbientGlowPulse.kt          — large pulsing radial glow (primary color)
    DriftingParticleField.kt     — N floating 2dp dots with stable Random seeds
    VignetteOverlay.kt           — Canvas radial vignette (Black allowed here only)
    FilmGrainOverlay.kt          — vertical gradient tiled with BlendMode.Overlay
    BreathingOrb.kt              — 3 concentric rings (1x / 0.78x / 0.50x) + halo
    PillButton.kt                — PulsingTapHint + ShimmerPillButton (shimmer drawWithContent)
    StepIndicator.kt             — "— N / 3 —" label
    AnimatedFadeIn.kt            — fade-only entrance helper
    AnimatedFadeUp.kt            — fade + 12dp lift entrance helper
  steps/
    WelcomeStep.kt               — screen 1: full-screen tap, wordmark, tagline, intro copy
    StepInsideStep.kt            — screen 2: BreathingOrb centerpiece
    PillarsStep.kt               — screen 3: 3 PillarCards + ENTER button
```

---

## MVI Contract

```kotlin
enum class OnboardingStep { Welcome, StepInside, Pillars }

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val isAdvancing: Boolean = false,
)

sealed interface OnboardingIntent {
    data object AdvanceStep : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
}
```

---

## Repository

```kotlin
class OnboardingRepository(private val store: JsonStore = JsonStore()) {
    private val _hasCompleted = MutableStateFlow(load().completed)
    val hasCompletedFlow: StateFlow<Boolean> = _hasCompleted.asStateFlow()

    fun markCompleted() { /* writes State(completed=true) */ }
    fun reset()         { /* writes State(completed=false), for debug toggle */ }

    @Serializable
    private data class State(val completed: Boolean = false)
    // KEY = "onboarding.state.v1"
}
```

---

## ViewModel

Constructor: `OnboardingRepository`, `SoundMixer`, `HapticController`, `AmbientStateRepository`

Key constants:
- `SOUND_WIND = "wind"`, `WIND_TARGET = 0.18f`, `WIND_FADE_MS = 2000`
- `SOUND_RAIN = "rain"`, `RAIN_TARGET = 0.30f`, `RAIN_FADE_MS = 1500`
- `FADE_STEP_MS = 50` — coroutine loop (SoundMixer has no native fade API)

Lifecycle:
- `init` → fades Wind 0 → 0.18 over 2 s
- `AdvanceStep` from Welcome → fades Rain 0 → 0.30 over 1.5 s
- `CompleteOnboarding` → writes `AmbientSnapshot(rain@0.30, wind@0.18)` to `AmbientStateRepository`, calls `markCompleted()`, emits `completedEvents: SharedFlow<Unit>` (extraBufferCapacity=1)

**Note:** The `SoundMixer` instance used here is a dedicated silent instance created in `App.kt` solely for onboarding. It does NOT share the Mixer screen's `SoundMixer`. Audio handoff is via the `AmbientSnapshot` written to disk; the Mixer screen hydrates from that snapshot when it initialises.

---

## Screen host

```kotlin
@Composable
fun OnboardingScreen(
    onboardingRepository: OnboardingRepository,
    soundMixer: SoundMixer,
    hapticController: HapticController,
    ambientStateRepository: AmbientStateRepository,
    onComplete: () -> Unit,
)
```

Creates ViewModel via `viewModel {}`, collects `completedEvents` → calls `onComplete()`.

---

## Screen content (step router)

```kotlin
AnimatedContent(
    targetState = state.currentStep,
    transitionSpec = {
        (fadeIn(tween(900, FastOutSlowInEasing)) + scaleIn(tween(900, FastOutSlowInEasing), initialScale = 0.96f))
            .togetherWith(fadeOut(tween(500)))
    }
) { step -> WelcomeStep / StepInsideStep / PillarsStep }
```

---

## Step 1 — WelcomeStep

Full-screen `Box`, clickable anywhere → `AdvanceStep`. No step indicator.

Layout (all `Box` layers stacked):
- `AtmosphericBackdrop(showForest=true, particleCount=5, glowIntensity=1f)`
- **Wordmark** "FocusRitual" — `AnimatedFadeUp(delay=0, duration=2400)`, offset y=−30dp, W200, 27sp, alpha 0.92
- **Tagline** "A QUIET SPACE" — `AnimatedFadeIn(delay=800, duration=3000)`, 10sp, W300, letterSpacing 0.32em, alpha 0.40
- **Body** "An ambient companion…" — `AnimatedFadeIn(delay=1400, duration=3000)`, 13sp, W300, lineHeight 22sp, alpha 0.60, padding bottom 130dp
- **PulsingTapHint** "TAP TO CONTINUE" — padding bottom 36dp

---

## Step 2 — StepInsideStep

Full-screen `Box`, clickable anywhere → `AdvanceStep`.

Layout:
- `AtmosphericBackdrop(showForest=false, particleCount=6, glowIntensity=0.7f)`
- `StepIndicator("— 2 / 3 —")` — top center, padding top 58dp
- `Column` padding top 150dp: "Step inside." (22sp W300, alpha 0.85) + "Breathe with the light.…" (11sp W300, alpha 0.40)
- `BreathingOrb(size=180.dp)` — centered, offset y=+60dp
- `PulsingTapHint("TAP TO CONTINUE")` — bottom 36dp

---

## Step 3 — PillarsStep

No tap-to-advance (only the ENTER button advances).

Layout:
- `AtmosphericBackdrop(showForest=true, particleCount=3, glowIntensity=0.8f)`
- `StepIndicator("— 3 / 3 —")` — top center, padding top 58dp
- Headline "A small space,\nthree ways to inhabit it." — 17sp W300, alpha 0.88, padding top 88dp
- `Column(verticalArrangement=spacedBy(10dp))` at padding top 180dp: 3 × `PillarCard`
- `ShimmerPillButton("ENTER")` — bottom 50dp → `CompleteOnboarding`

### PillarCard details

| Card   | Icon                       | Name   | Entrance delay |
|--------|----------------------------|--------|----------------|
| 1      | `Icons.Outlined.GraphicEq` | MIXER  | 400 ms         |
| 2      | `Icons.Outlined.Schedule`  | FOCUS  | 800 ms         |
| 3      | `Icons.Outlined.Bedtime`   | SLEEP  | 1200 ms        |

Each card: `animateFloatAsState` alpha 0→1 + `animateDpAsState` offsetY 8dp→0 over 800ms FastOutSlowInEasing on `visible` flag set after delay. Halo ring uses `rememberInfiniteTransition` with `StartOffset(entranceDelayMs)` for phase stagger (scale 0.95→1.10, alpha 0.2→0.7, 5000ms). Card background `onSurface@0.05`, border `onSurface@0.10`, radius 14dp.

---

## Components

### AtmosphericBackdrop
Stacks (bottom → top): surface background → ForestKenBurnsLayer (optional) → DriftingMistLayer → AmbientGlowPulse → DriftingParticleField → VignetteOverlay → FilmGrainOverlay.

### ForestKenBurnsLayer
`Res.drawable.background` image, slow Ken Burns: scale 1.0→1.06 + translateY 0→−6px, 24 s linear, RepeatMode.Reverse.

### DriftingMistLayer
Canvas. Two overlapping radial gradients (`onSurface@0.05`) blended with `BlendMode.Screen`. Horizontal drift −12→14dp over 22 s.

### AmbientGlowPulse
340dp circle, radial gradient center `primary@(0.06×intensity)` → transparent. Scale 0.92→1.08, alpha 0.6→1.0, 7 s FastOutSlowInEasing, Reverse.

### DriftingParticleField
`count` particles. Each has stable `Random` seeds stored in `remember(count)`. Each particle: 2dp circle, `softBlur(0.3dp)`, drifts via `IntOffset`, fades alpha 0→maxAlpha (0.3–0.8), 11–16 s, phase-offset 0–5 s.

### VignetteOverlay
Canvas radial gradient: transparent at 0.3 → `Color.Black@0.65` at 1.0. Radius = 0.75× max(w,h). (`Color.Black` is permitted only here.)

### FilmGrainOverlay
Canvas. 3px-stride vertical gradient tiled with `TileMode.Repeated`, `BlendMode.Overlay`, `onSurface@0.025`.

### BreathingOrb (`size: Dp`, `intensity: Float = 1f`)
- Halo: `size×1.5`, `softBlur(28dp)`, radial gradient `primary@(0.10×intensity)` → transparent
- Outer ring: `size`, border `onSurface@(0.10×intensity)`, scale 0.96→1.04, 7200ms `OrganicEasing`
- Middle ring: `size×0.78`, border `onSurface@(0.14×intensity)`, scale 0.97→1.03, 4800ms FastOutSlowInEasing
- Core: `size×0.50`, radial fill `onSurface@(0.16×intensity)` → `onSurface@(0.04×intensity)` → transparent, border `onSurface@(0.20×intensity)`, scale 0.98→1.02, 4800ms

### PulsingTapHint
Text label, alpha pulses 0.25→0.65 over 3 s FastOutSlowInEasing Reverse. 9sp W300, letterSpacing 0.22em.

### ShimmerPillButton
Pill shape (radius 24dp), `onSurface@0.10` fill, `onSurface@0.16` border, `drawWithContent` sweeping shimmer `onSurface@0.08`. Scale 0.97 on press. 10sp W300, letterSpacing 0.2em, text `onSurface@0.88`.

### AnimatedFadeIn / AnimatedFadeUp
Entrance helpers: `LaunchedEffect(Unit)` sets `visible = true` after `delayMs`. FadeUp also translates from +12dp.

### StepIndicator
Small text label: 8.5sp W300, letterSpacing 0.3em, `onSurface@0.30`.

---

## App.kt wiring

```kotlin
// First launch detection (read once)
val onboardingRepository = remember { OnboardingRepository() }
val initialScreen = remember {
    if (onboardingRepository.hasCompletedFlow.value) AppScreen.Mixer else AppScreen.Onboarding
}
val seedDefaults = remember { onboardingRepository.hasCompletedFlow.value }

// Dedicated silent SoundMixer for onboarding audio fade logic
val onboardingSoundMixer = remember { SoundMixer() }

// MixerViewModel gets seedDefaultsIfEmpty=false when onboarding hasn't run yet
val mixerViewModel = viewModel {
    MixerViewModel(..., seedDefaultsIfEmpty = seedDefaults)
}

// On Onboarding screen branch:
OnboardingScreen(
    onboardingRepository = onboardingRepository,
    soundMixer = onboardingSoundMixer,
    hapticController = hapticController,
    ambientStateRepository = ambientStateRepository,
    onComplete = {
        mixerViewModel.reloadAmbientSnapshot()  // picks up rain+wind snapshot from disk
        currentScreen = AppScreen.Mixer
    }
)

// Transition: fadeIn(800ms) + scaleIn(0.96f, EaseOutCubic) enter / fadeOut(600ms) + scaleOut(1.02f) exit
```

---

## Design constraints

- Dark-only. All colors via `MaterialTheme.colorScheme.*` — no `Color(0xFF…)`, no `Color.White`. Exception: `Color.Black` in `VignetteOverlay` only.
- Font weights: W200 (wordmark), W300 (body/labels), W400 (cards).
- No ripple on any clickable (`indication = null`).
- Press feedback: `scale(0.97f)` via `animateFloatAsState`.
- `softBlur` is an `expect/actual` shim — on Android it gates on API ≥ 31; on iOS it runs unconditionally.
- `OrganicEasing` lives at `core/designsystem/Motion.kt` (shared).

---

## Debug toggle

Settings sheet → **Debug** section → "Show onboarding on next launch" switch.
- ON → calls `onboardingRepository.reset()` → next cold launch shows onboarding.
- OFF → calls `onboardingRepository.markCompleted()` → next cold launch goes to Mixer.
- Subtitle: "Relaunch app to take effect".
