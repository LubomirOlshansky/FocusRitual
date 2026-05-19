# Onboarding Feature — Implementation Overview

Kotlin Multiplatform / Compose Multiplatform. All UI in `commonMain`. Dark-only design system.
All onboarding strings live in `composeResources/values/strings.xml` under `onboarding_*` keys.

---

## File Map

```
feature/onboarding/
  OnboardingContract.kt          — MVI state/intent types
  OnboardingViewModel.kt         — business logic, audio fade, completion signal
  OnboardingScreen.kt            — stateful host (creates VM, listens for completion)
  OnboardingScreenContent.kt     — stateless, AnimatedContent step router
  data/
    OnboardingRepository.kt      — JsonStore-backed persistence (hasCompleted flag + reset)
  components/
    AtmosphericBackdrop.kt       — stacks all background layers
    ForestKenBurnsLayer.kt       — slow scale+translateY on `background` drawable
    DriftingMistLayer.kt         — Canvas radial gradients, horizontal drift
    AmbientGlowPulse.kt          — large pulsing radial glow (primary color)
    DriftingParticleField.kt     — N floating 2dp dots with stable Random seeds
    VignetteOverlay.kt           — Canvas radial vignette (Color.Black allowed here only)
    FilmGrainOverlay.kt          — vertical gradient tiled with BlendMode.Overlay
    BreathingOrb.kt              — 3 concentric rings (1x / 0.78x / 0.50x) + halo
    DistantLight.kt              — tiny pinpoint of light used on Welcome screen (7200ms breath)
    PillButton.kt                — PulsingTapHint (no ShimmerPillButton — replaced by BreathingPillButton)
    BreathingPillButton.kt       — breathing primary-halo ENTER button (4800ms synced to orb)
    AnimatedFadeIn.kt            — fade-only entrance helper
    AnimatedFadeUp.kt            — fade + 12dp lift entrance helper
  steps/
    WelcomeStep.kt               — screen 1: two-anchored-column layout + DistantLight at true center
    StepInsideStep.kt            — screen 2: orb at true center + orb-morph exit animation
    PillarsStep.kt               — screen 3: entry glow, cards in upper third, ENTER in lower third
```

---

## Strings

All onboarding copy lives in `composeResources/values/strings.xml`. Keys:

| Key | Value |
|-----|-------|
| `onboarding_welcome_wordmark` | FocusRitual |
| `onboarding_welcome_tagline` | A QUIET SPACE |
| `onboarding_welcome_body` | A space for focus.\nA space for rest. |
| `onboarding_tap_to_continue` | TAP TO CONTINUE |
| `onboarding_step_inside_title` | Step inside. |
| `onboarding_step_inside_subtitle` | Breathe with the light. |
| `onboarding_tap_the_light` | TAP THE LIGHT |
| `onboarding_pillars_headline` | Your space.\nYour rhythm. |
| `onboarding_pillar_atmosphere_name` | ATMOSPHERE |
| `onboarding_pillar_atmosphere_desc` | Build your space |
| `onboarding_pillar_ritual_name` | RITUAL |
| `onboarding_pillar_ritual_desc` | Your daily practice |
| `onboarding_pillar_focus_name` | FOCUS |
| `onboarding_pillar_focus_desc` | Deep work, undisturbed |
| `onboarding_pillar_rest_name` | REST |
| `onboarding_pillar_rest_desc` | Drift into silence |
| `onboarding_enter` | ENTER |

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
    fun reset()         { /* writes State(completed=false) — used by debug toggle */ }

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
- `AdvanceStep` from Welcome → fades Rain 0 → 0.30 over 1.5 s; fires `hapticController.onboardingAdvance()`
- `CompleteOnboarding` → fires `hapticController.onboardingComplete()`, writes `AmbientSnapshot(rain@0.30, wind@0.18)` to `AmbientStateRepository`, calls `markCompleted()`, emits `completedEvents: SharedFlow<Unit>` (extraBufferCapacity=1)

**Audio handoff note:** Onboarding uses a *dedicated silent* `SoundMixer` created in `App.kt`. It does not share the Mixer screen's instance. Rain+wind state is persisted as `AmbientSnapshot` on disk; the Mixer screen hydrates from that snapshot via `reloadAmbientSnapshot()` when it initialises.

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

## Screen content — step router & transitions

```kotlin
AnimatedContent(
    targetState = state.currentStep,
    transitionSpec = {
        if (initialState == OnboardingStep.StepInside && targetState == OnboardingStep.Pillars) {
            // Wide cross-fade — orb expansion + entry glow carry the visual morph
            fadeIn(tween(900)).togetherWith(fadeOut(tween(900)))
        } else {
            // Standard enter: fade+scale / exit: fade
            (fadeIn(tween(900, FastOutSlowInEasing)) +
                scaleIn(tween(900, FastOutSlowInEasing), initialScale = 0.96f))
                .togetherWith(fadeOut(tween(500)))
        }
    }
)
```

---

## Animation rhythm table

| Screen | Element | Delay (ms) | Duration (ms) | Easing |
|--------|---------|-----------|---------------|--------|
| 1 | Wordmark (FadeUp) | 0 | 2400 | FastOutSlowIn |
| 1 | Hairline accent (FadeIn) | 600 | 2400 | FastOutSlowIn |
| 1 | Tagline (FadeIn) | 800 | 3000 | FastOutSlowIn |
| 1 | Body copy + scrim (FadeIn) | 1400 | 3000 | FastOutSlowIn |
| 1 | **DistantLight (FadeIn)** | **2400** | **4000** | FastOutSlowIn |
| 1 | DistantLight breath | — | 7200 loop | OrganicEasing Reverse |
| 1 | TAP TO CONTINUE (pulse) | — | 2800 loop | FastOutSlowIn |
| 2 | "Step inside." (FadeIn) | 200 | 1800 | FastOutSlowIn |
| 2 | "Breathe with the light." (FadeIn) | 800 | 2400 | FastOutSlowIn |
| 2 | TAP THE LIGHT label (pulse) | — | 7200 loop | FastOutSlowIn |
| 2 | Outer pulse ring (infinite) | — | 4800 loop Restart | FastOutSlowIn |
| 2→3 | Orb expand on tap | 0 | **1500** | OrganicEasing |
| 2→3 | Content alpha fade-out | 0 | **500** | — |
| 2→3 | First haptic (onboardingAdvance) | 0 | — | — |
| 2→3 | Second haptic + onAdvance() fires | **900** | — | — |
| 2→3 | Screen transition | 0 | 900 | fade only |
| 3 | Entry glow dissipate | 0 | 2200 | OrganicEasing |
| 3 | Headline (FadeIn) | 200 | 1800 | FastOutSlowIn |
| 3 | Card 1 ATMOSPHERE | 400 | 800 | FastOutSlowIn |
| 3 | Card 2 RITUAL | 700 | 800 | FastOutSlowIn |
| 3 | Card 3 FOCUS | 1000 | 800 | FastOutSlowIn |
| 3 | Card 4 REST | 1300 | 800 | FastOutSlowIn |
| 3 | ENTER button (fadeIn) | 1700 | 1200 | — |
| 3 | ENTER halo breath | — | 4800 loop | FastOutSlowIn Reverse |
| 3 | ENTER outer pulse ring (infinite) | — | 4800 loop Restart | FastOutSlowIn |

---

## Step 1 — WelcomeStep

Tap anywhere on screen → `AdvanceStep`. No step indicator.

The **light motif begins here**: a small DistantLight at true screen center fades in after the text has settled (2400ms delay), establishing a distant focal point in the forest.

```
Box (fillMaxSize, clickable anywhere)
├── AtmosphericBackdrop(showForest=true, particleCount=5, glowIntensity=1f)
│
├── Column (Alignment.TopCenter, padding top 110dp, CenterHorizontally)
│   ├── AnimatedFadeUp(0ms, 2400ms)   →  "FocusRitual"  34sp W200  alpha 0.92
│   ├── Spacer 12dp
│   ├── AnimatedFadeIn(600ms, 2400ms) →  Box 28×0.5dp  onSurface@0.20  (hairline)
│   ├── Spacer 14dp
│   └── AnimatedFadeIn(800ms, 3000ms) →  "A QUIET SPACE"  11sp W300  ls 0.32em  alpha 0.42
│
├── Box (Alignment.Center)   ← NO vertical offset — true screen center
│   └── AnimatedFadeIn(2400ms, 4000ms)
│       └── DistantLight()   96dp halo + 6dp core, 7200ms OrganicEasing breath
│
└── Column (Alignment.BottomCenter, padding bottom 56dp, CenterHorizontally)
    └── AnimatedFadeIn(1400ms, 3000ms)
        ├── Box (contentAlignment=Center)   ← scrim + body stacked
        │   ├── SCRIM  Box 260×72dp  softBlur(16dp)
        │   │          radialGradient(surface@0.55 → Transparent)
        │   └── Text  "A space for focus.\nA space for rest."
        │             16sp lh 24sp W300  textAlign Center  alpha 0.78
        ├── Spacer 36dp
        └── PulsingTapHint("TAP TO CONTINUE")
```

---

## Step 2 — StepInsideStep

**The orb is the only tap target.** No bottom hint, no step indicator.

### Exit animation (orb-morph)

On orb tap → sets `isExiting = true`:
- `orbSize` 240dp → **1100dp**, tween(**1500**) **OrganicEasing**
- `orbIntensity` 1f → **3.5f**, tween(**1500**) **OrganicEasing**
- `contentAlpha` 1f → 0f, tween(**500**)
- Immediate: `hapticController.onboardingAdvance()` (first)
- After **900ms** delay: `hapticController.onboardingAdvance()` (second) → `onAdvance()` fires

**Cross-fade overlap:** `onAdvance()` fires at 900ms while the orb is still expanding (1500ms total). The screen cross-fade (900ms) starts during the orb bloom — the user perceives one continuous brightening into Pillars.

**True-center orb:** No `orbOffsetY`. Orb sits at the exact same `Alignment.Center` as the Welcome screen's `DistantLight`. The 1→2 cross-fade reads as one light growing in place.

```
Box (fillMaxSize)
├── AtmosphericBackdrop(showForest=false, particleCount=15, glowIntensity=0.85f)
├── Canvas (fillMaxSize)   ← outer pulse ring (drawn behind orb)
│   pulseScale 1→2.4, pulseAlpha 0.25→0, 4800ms Restart FastOutSlowIn
│   center = (width/2, height/2)   ← true center, NO orbOffsetY
│   radius = 120.dp × pulseScale,  stroke 0.5dp  onSurface@(pulseAlpha × contentAlpha)
├── Column (padding top 110dp, .alpha(contentAlpha), CenterHorizontally)
│   ├── AnimatedFadeIn(200ms, 1800ms) → "Step inside."  30sp W300  alpha 0.88
│   ├── Spacer 16dp
│   └── AnimatedFadeIn(800ms, 2400ms) → "Breathe with the light."  13sp lh 21sp W300  alpha 0.48
├── Box (size 320dp, Alignment.Center — NO offset, clickable → orb-morph + advance)
│   └── BreathingOrb(size = orbSize, intensity = orbIntensity)
└── "TAP THE LIGHT"  10sp W300  ls 0.28em  alpha = labelAlpha×contentAlpha
    labelAlpha: 0.20→0.55 over 7200ms FastOutSlowIn (synced to outer ring breath)
    aligned BottomCenter  pb 80dp
```

---

## Step 3 — PillarsStep

Opens with a dissipating primary glow completing the orb-morph visual.

### Entry glow

`Animatable(1f)` → animateTo(0f, tween(**2200**, easing = **OrganicEasing**)) on `LaunchedEffect(Unit)`.

Canvas draws a radial gradient at center:
- Stops: `primary@(0.35×glow)` → `primary@(0.08×glow)` → `Transparent`
- Radius: `minDimension × 0.7 × (1.2 + (1−glow) × 0.6)` — expands slightly as it fades

**Darker, more intimate.** `glowIntensity` drops to 0.45 (was 0.6). Cards anchor to the upper third. ENTER dominates the lower third — the light motif's final form.

```
Box (fillMaxSize)
├── AtmosphericBackdrop(showForest=true, particleCount=3, glowIntensity=0.45f)
├── Canvas (entry glow — 2200ms OrganicEasing dissipation)
├── Column (padding top 88dp, CenterHorizontally)
│   └── AnimatedFadeIn(200ms, 1800ms) → "Your space.\nYour rhythm."
│                                        22sp lh 30sp W300  alpha 0.90
├── Box (Alignment.TopCenter, padding top 160dp, padding h 16dp)
│   ├── SCRIM  matchParentSize  radialGradient(surface@0.55 → Transparent)
│   └── Column (fillMaxWidth, padding h 8dp, spacedBy 8dp)
│       ├── PillarCard("ATMOSPHERE", "Build your space",       GraphicEq,       400ms)
│       ├── PillarCard("RITUAL",     "Your daily practice",    SelfImprovement, 700ms)
│       ├── PillarCard("FOCUS",      "Deep work, undisturbed", Schedule,        1000ms)
│       └── PillarCard("REST",       "Drift into silence",     Bedtime,         1300ms)
└── AnimatedVisibility(enterVisible @ 1700ms, fadeIn 1200ms)  aligned BottomCenter  pb 80dp
    └── BreathingPillButton("ENTER", onClick = onComplete)
```

### PillarCard

Per-card entrance: `visible` flag set after `entranceDelayMs`. `animateFloatAsState` alpha 0→1 + `animateDpAsState` offsetY 8dp→0, both tween(800) FastOutSlowInEasing.

Visual:
- Background `onSurface@0.10`, border 0.5dp `onSurface@0.22`, radius 16dp
- `drawWithContent` draws a top highlight line: `onSurface@0.28`, y=0.5px, x 20→(width−20)
- Padding horizontal 18dp, vertical 16dp
- Icon 22dp `onSurface@0.70` | name 13sp W400 ls 0.12em `onSurface@0.92` | desc 12sp W300 `onSurface@0.55`

---

## Components

### AtmosphericBackdrop
Layer stack (bottom → top):
`surface bg` → `ForestKenBurnsLayer` (if showForest) → `DriftingMistLayer` → `AmbientGlowPulse` → `DriftingParticleField` → `VignetteOverlay` → `FilmGrainOverlay`

### ForestKenBurnsLayer
`Res.drawable.background`, ContentScale.Crop. Scale 1.0→1.06 + translateY 0→−6px, 24 s Linear, Reverse.

### DriftingMistLayer
Canvas, two `BlendMode.Screen` radial gradients (`onSurface@0.05`). Horizontal drift −12→14dp, 22 s Linear, Reverse.

### AmbientGlowPulse
340dp Circle. Radial gradient `primary@(0.06×intensity)` → transparent. Scale 0.92→1.08 + alpha 0.6→1.0, 7 s FastOutSlowIn, Reverse.

### DriftingParticleField
`count` particles, seeds stable in `remember(count)`. Each: 2dp circle, `softBlur(0.3dp)`, drifts via `IntOffset`, alpha 0→maxAlpha (0.3–0.8), 11–16 s, phase-offset 0–5 s.

### VignetteOverlay
Canvas radial gradient: transparent@0.3 → `Color.Black@0.65`@1.0. Radius = 0.75× max(w,h). (`Color.Black` permitted only here.)

### FilmGrainOverlay
Canvas. 3px-stride vertical gradient tiled `TileMode.Repeated`, `BlendMode.Overlay`, `onSurface@0.025`.

### BreathingOrb (`size: Dp`, `intensity: Float = 1f`)

All animations loop forever (Reverse):

| Layer | Size | Animation | Duration | Easing |
|-------|------|-----------|----------|--------|
| Halo | size×1.5 | none (static) | — | — |
| Outer ring | size | scale 0.96→1.04 | 7200ms | OrganicEasing |
| Middle ring | size×0.78 | scale 0.97→1.03 | 4800ms | FastOutSlowIn |
| Core | size×0.50 | scale 0.98→1.02 | 4800ms | FastOutSlowIn |

- Halo: `softBlur(28dp)`, radial gradient `primary@(0.14×intensity)` → transparent
- Outer border: `onSurface@(0.10×intensity)`
- Middle border: `onSurface@(0.14×intensity)`
- Core: radial fill `onSurface@(0.16×intensity)` center → `onSurface@(0.04×intensity)` → transparent; border `onSurface@(0.20×intensity)`

### PulsingTapHint
Alpha 0.42→0.88, 2800ms FastOutSlowIn Reverse. 10sp W300 letterSpacing 0.24em. Used on screen 1 only.

### BreathingPillButton
Used only for the ENTER button on screen 3. Five infinite animations on the same transition (all 4800ms, same period as orb middle/core):

| Animated value | Range | RepeatMode |
|----------------|-------|------------|
| `haloScale` | 0.92 → 1.20 | Reverse |
| `haloAlpha` | 0.26 → 0.54 | Reverse |
| `pillScale` | 0.97 → 1.03 | Reverse |
| `pulseScale` | 1.0 → 1.8 | Restart |
| `pulseAlpha` | 0.22 → 0 | Restart |

Layout (Box, Alignment.Center — layers bottom → top):
1. **Outer pulse ring** — `Canvas(320dp)`: circle stroke 0.5dp `primary@pulseAlpha`, radius `(size.minDimension/2) × pulseScale`
2. **Halo** — 320dp Box, `scale(haloScale)`, `softBlur(28dp)`, radial gradient `primary@haloAlpha` → transparent, radius 400f
3. **Pill** — `clip(RoundedCornerShape(28dp))`, background `onSurface@0.10`, border 0.5dp `onSurface@0.28`

- Press: `pointerInput + detectTapGestures`, pressScale 1f→0.97f over 120ms. No ripple.
- Text: label **13sp** W300 ls 0.22em `onSurface@0.95`, padding h **52dp** v **16dp**

### DistantLight
Used only on the Welcome screen. A small pinpoint of atmospheric light that establishes the **light motif** — the same element that grows into the full orb on screen 2 and is internalized as the ENTER halo on screen 3.

| Layer | Size | Description |
|-------|------|-------------|
| Halo | 96dp | `softBlur(24dp)`, radial gradient `primary@(0.14×intensity)` → `primary@(0.05×intensity)` → transparent |
| Core | 6dp | `CircleShape`, `onSurface@(0.55×intensity)` |

Both layers animate on the same infinite transition: `scale` 0.94→1.08 and `intensity` 0.82→1.0, both 7200ms `OrganicEasing` Reverse — matching the BreathingOrb outer ring's period exactly.

### AnimatedFadeIn / AnimatedFadeUp
`LaunchedEffect(Unit)` delays then sets `visible = true`. FadeUp adds 12dp Y translate. Both use FastOutSlowInEasing.

---

## App.kt wiring

```kotlin
val onboardingRepository = remember { OnboardingRepository() }
val ambientStateRepository = remember { AmbientStateRepository() }
val onboardingSoundMixer = remember { SoundMixer() }   // silent, onboarding only

val initialScreen = remember {
    if (onboardingRepository.hasCompletedFlow.value) AppScreen.Mixer else AppScreen.Onboarding
}
val seedDefaults = remember { onboardingRepository.hasCompletedFlow.value }

val mixerViewModel = viewModel {
    MixerViewModel(..., seedDefaultsIfEmpty = seedDefaults)
}

// Onboarding branch:
OnboardingScreen(
    onboardingRepository = onboardingRepository,
    soundMixer = onboardingSoundMixer,
    hapticController = hapticController,
    ambientStateRepository = ambientStateRepository,
    onComplete = {
        mixerViewModel.reloadAmbientSnapshot()   // picks up rain+wind from disk
        currentScreen = AppScreen.Mixer
    }
)

// Transition into Mixer:
// Enter: fadeIn(800ms) + scaleIn(0.96f, EaseOutCubic)
// Exit:  fadeOut(600ms) + scaleOut(1.02f)
```

---

## Design constraints

- Dark-only. All colors via `MaterialTheme.colorScheme.*`. No `Color(0xFF…)`, no `Color.White`. `Color.Black` only in `VignetteOverlay`.
- Font weights: W200 (wordmark), W300 (body, labels, button), W400 (card names).
- Border thickness: 0.5dp everywhere.
- No ripple on any clickable (`indication = null`). ENTER uses `pointerInput + detectTapGestures`.
- Press feedback: scale 0.97 via `animateFloatAsState(120ms)`.
- `softBlur`: `expect/actual` shim — Android gates on API ≥ 31; iOS unconditional.
- `OrganicEasing` at `core/designsystem/theme/Motion.kt` (import: `com.focusritual.app.core.designsystem.theme.OrganicEasing`).

---

## Debug toggle

Settings → **Debug** section → "Show onboarding on next launch" switch.
- **ON** → `onboardingRepository.reset()` → next cold launch shows onboarding.
- **OFF** → `onboardingRepository.markCompleted()` → next cold launch skips to Mixer.
- Subtitle: "Relaunch app to take effect".
