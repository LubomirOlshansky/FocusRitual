# Onboarding — First-Launch Experience

## 1. Goal

A three-screen first-launch onboarding that establishes the FocusRitual sanctuary aesthetic before the user reaches the Mixer. The sequence opens in near-silence, gently introduces ambient wind, then layers in rain as the wordmark dissolves into a breathing orb, finally presenting three pillar cards (Mix · Focus · Protect) and a single "Enter the Sanctuary" call-to-action. After completion the user is permanently routed to the Mixer with the faded-in ambient mix carried over — never seeing onboarding again unless app data is cleared.

## 2. Scope & Non-Goals

**In scope**
- Three screen states (Approach → Inside → Pillars) implemented as one `OnboardingScreen` with `AnimatedContent`.
- `OnboardingViewModel` + `OnboardingContract` (MVI).
- `OnboardingRepository` backed by `JsonStore` exposing `hasCompletedFlow: Flow<Boolean>` and `suspend fun markCompleted()`.
- Audio fade-in (Wind → 0.18 on Screen 1, Rain → 0.30 on Screen 2) driven from the VM.
- Default-startup gating: when `hasCompleted == false`, Mixer defaults must NOT auto-enable Rain/Wind so onboarding can start silent.
- Ambient handoff: persist the faded-in mix to `AmbientStateRepository` before completion so Mixer inherits the actual onboarding mix (not snapping to its own defaults).
- Navigation gating in `App.kt`: splash black `Box` until first emit of `hasCompletedFlow`, then route to `Onboarding` or `Mixer`.
- Onboarding → Mixer crossfade transition.

**Out of scope (future plans)**
- Live Activity permission sheet (separate plan).
- Push-notification permission prompt.
- A Settings entry to replay onboarding (note: a "Reset onboarding" toggle in dev settings is a sensible follow-up).

## 3. User-Confirmed Decisions

- **Repository:** new `OnboardingRepository` (JsonStore-backed), same pattern as `MixPresetRepository` / `AmbientStateRepository`.
- **Audio gating:** default startup checks `hasCompleted`; if false, Rain & Wind start at volume 0 / disabled. Onboarding VM fades Wind (Screen 1, target 0.18) and Rain (Screen 2, target 0.30) in over time. These targets are LOWER than the normal Mixer defaults (Rain 0.70 / Wind 0.50) — therefore the VM persists the faded-in mix to `AmbientStateRepository` before completion so Mixer inherits the faded values rather than snapping to defaults.
- **BreathingOrb location:** `feature/onboarding/components/BreathingOrb.kt`. The active-session timer orb is NOT touched.
- **`OrganicEasing` location:** ALREADY exists at [composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt#L23](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt#L23). NO move required. Onboarding components import from there. (The user-provided spec assumed it lived in `feature/timer/AtmosphericField.kt`; that file moved already and is now at [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/AtmosphericField.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/AtmosphericField.kt) and already imports `OrganicEasing` from `core.designsystem.theme`.)
- **Live Activity permission sheet:** out of scope.
- **Splash gate:** while `hasCompletedFlow.collectAsState(initial = null)` is null, render a full-screen `Box` painted with `MaterialTheme.colorScheme.surface`.
- **Material Icons Extended:** already a dependency — verified at [composeApp/build.gradle.kts#L38](composeApp/build.gradle.kts#L38) (`implementation(compose.materialIconsExtended)`). No action required.

## 4. Design-System Mapping Table

The spec uses raw `Color.White.copy(alpha = X)` and several `Color(0xFF…)` literals. Per project rules ([design-system memory], no `Color.White`/`Color.Black`/hex literals in feature code), every value below maps 1:1 to a `MaterialTheme.colorScheme.*` token, preserving the original alpha.

### `Color.White.copy(alpha = X)` → `MaterialTheme.colorScheme.onSurface.copy(alpha = X)`

| Spec alpha | Replacement |
|---|---|
| 0.04 | `colorScheme.onSurface.copy(alpha = 0.04f)` |
| 0.05 | `colorScheme.onSurface.copy(alpha = 0.05f)` |
| 0.06 | `colorScheme.onSurface.copy(alpha = 0.06f)` |
| 0.08 | `colorScheme.onSurface.copy(alpha = 0.08f)` |
| 0.10 | `colorScheme.onSurface.copy(alpha = 0.10f)` |
| 0.14 | `colorScheme.onSurface.copy(alpha = 0.14f)` |
| 0.16 | `colorScheme.onSurface.copy(alpha = 0.16f)` |
| 0.20 | `colorScheme.onSurface.copy(alpha = 0.20f)` |
| 0.22 | `colorScheme.onSurface.copy(alpha = 0.22f)` |
| 0.25 | `colorScheme.onSurface.copy(alpha = 0.25f)` |
| 0.30 | `colorScheme.onSurface.copy(alpha = 0.30f)` |
| 0.32 | `colorScheme.onSurface.copy(alpha = 0.32f)` |
| 0.40 | `colorScheme.onSurface.copy(alpha = 0.40f)` |
| 0.45 | `colorScheme.onSurface.copy(alpha = 0.45f)` |
| 0.60 | `colorScheme.onSurface.copy(alpha = 0.60f)` |
| 0.65 | `colorScheme.onSurface.copy(alpha = 0.65f)` |
| 0.70 | `colorScheme.onSurface.copy(alpha = 0.70f)` |
| 0.85 | `colorScheme.onSurface.copy(alpha = 0.85f)` |
| 0.88 | `colorScheme.onSurface.copy(alpha = 0.88f)` |
| 0.92 | `colorScheme.onSurface.copy(alpha = 0.92f)` |

### Surface / background literals

| Spec literal | Replacement | Notes |
|---|---|---|
| `Color(0xFF060709)` | `colorScheme.surface` | Darkest backdrop fill (root Box, vignette deepest stop) |
| `Color(0xFF0a0d12)` | `colorScheme.surface` | Same role — base sanctuary black |
| `Color(0xFF0e1218)` | `colorScheme.surfaceContainer` | Subtle elevated card fill (PillarCard background) |
| `Color(0xFF12161e)` | `colorScheme.surfaceContainerHigh` | Slightly raised tint (only if a second-level elevation is needed) |
| `Color.Black` (vignette outer stop) | `colorScheme.surface` | Surface is already near-black; vignette deepens via overlay alpha, not pure black |

### Accent literals

| Spec literal | Replacement | Notes |
|---|---|---|
| `Color(0xFFB4C8E6)` (orb halo) | `colorScheme.primary` | Project primary is `#b7c8db`, intentionally close. Use at the spec's alpha (e.g. `primary.copy(alpha = 0.40f)`). |
| `Color(0xFF7896B4)` (ambient glow pulse) | `colorScheme.primary` | Same accent; use at the spec's alpha (`primary.copy(alpha = 0.06f..0.10f)`). |

### Hard rules (must hold in every new file)
- No `Color.White`, no `Color.Black`, no `Color(0xFF…)`.
- No ripple anywhere: every `clickable { … }` uses `indication = null, interactionSource = remember { MutableInteractionSource() }`.
- Font weights restricted to `W200` (wordmark only), `W300`, `W400`. Never higher.

## 5. File Structure

```
composeApp/src/commonMain/kotlin/com/focusritual/app/
  App.kt                                              [MODIFIED]  add Onboarding route + splash gate + transition

  feature/onboarding/                                 [NEW DIR]
    OnboardingContract.kt                             [NEW]  UiState, Intent, OnboardingStep
    OnboardingViewModel.kt                            [NEW]  MVI VM; owns audio fade-in + completion
    OnboardingScreen.kt                               [NEW]  stateful entry; collects VM, hosts OnboardingScreenContent
    OnboardingScreenContent.kt                        [NEW]  stateless; AnimatedContent across steps
    data/
      OnboardingRepository.kt                         [NEW]  JsonStore-backed hasCompletedFlow / markCompleted
    components/
      AtmosphericBackdrop.kt                          [NEW]  Box hosting all background layers (parameterised)
      ForestKenBurnsLayer.kt                          [NEW]  optional very slow scale/translate of forest image
      DriftingMistLayer.kt                            [NEW]  two soft radial gradients drifting across viewport
      AmbientGlowPulse.kt                             [NEW]  central low-alpha primary glow that breathes
      DriftingParticleField.kt                        [NEW]  N particles with stable seed (Particle data class)
      VignetteOverlay.kt                              [NEW]  radial gradient darkening edges
      FilmGrainOverlay.kt                             [NEW]  static painted brush grain (NOT animated)
      BreathingOrb.kt                                 [NEW]  scale + halo alpha breathing, OrganicEasing
      AnimatedFadeUp.kt                               [NEW]  utility: fade + translateY entrance with delay
      AnimatedFadeIn.kt                               [NEW]  utility: fade-only entrance with delay
      PulsingTapHint.kt                               [NEW]  small "Tap anywhere" indicator that pulses
      StepIndicator.kt                                [NEW]  3-dot horizontal indicator (active dot wider)
      ShimmerPillButton.kt                            [NEW]  primary CTA; gentle shimmer sweep across surface
      PillarCard.kt                                   [NEW]  icon + title + subtitle card for Pillars step
    steps/
      StepApproach.kt                                 [NEW]  Screen 1
      StepInside.kt                                   [NEW]  Screen 2
      StepPillars.kt                                  [NEW]  Screen 3

  feature/mixer/data/AmbientStateRepository.kt        [POSSIBLY MODIFIED]  see §10 for default-startup gating wiring
  feature/mixer/domain/MixRepository.kt               [POSSIBLY MODIFIED]  defaults must respect OnboardingRepository
```

**Not moving `OrganicEasing`** — already shared in [core/designsystem/theme/Motion.kt#L23](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt#L23). Onboarding components import `com.focusritual.app.core.designsystem.theme.OrganicEasing`.

## 6. MVI Contract

```kotlin
// OnboardingContract.kt
enum class OnboardingStep { Approach, Inside, Pillars }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Approach,
    val isCompleting: Boolean = false,
)

sealed interface OnboardingIntent {
    data object AdvanceStep : OnboardingIntent     // tap anywhere on Approach / Inside
    data object CompleteOnboarding : OnboardingIntent  // Pillars CTA
}
```

### `OnboardingViewModel` responsibilities

Constructor:
```kotlin
class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val ambientRepo: AmbientStateRepository,
    private val soundMixer: SoundMixer,
    private val hapticController: HapticController,
) : ViewModel()
```

- `init { … }` → launches a coroutine that begins the Wind fade-in to `0.18f` over `2000ms` (see §10 — implemented as a VM-side `Animatable`/loop driving `soundMixer.syncState(...)` since `SoundMixer` has no native fade API).
- `onIntent(AdvanceStep)` → emits `HapticFeedbackType.Light`; if current step is `Approach` → moves to `Inside` and starts Rain fade-in to `0.30f` over `1500ms`. If `Inside` → moves to `Pillars` (no audio change).
- `onIntent(CompleteOnboarding)` → emits `HapticFeedbackType.Medium`; sets `isCompleting = true`; (a) writes the current ambient state (Rain at 0.30, Wind at 0.18, both enabled) to `AmbientStateRepository`; (b) calls `repository.markCompleted()`; (c) the screen-level callback `onComplete()` triggers `App.kt` to switch route.

**DI / wiring spot.** ViewModels in this project are constructed inside `App.kt` via `viewModel { … }` blocks (see [App.kt#L57-L70](composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt#L57-L70)). The onboarding VM is wired the same way:
```kotlin
val onboardingViewModel: OnboardingViewModel = viewModel {
    OnboardingViewModel(
        repository = onboardingRepository,
        ambientRepo = AmbientStateRepository(),
        soundMixer = /* shared SoundMixer instance — see §10 */,
        hapticController = hapticController,
    )
}
```
`onboardingRepository` is a `remember { OnboardingRepository() }` at the top of `App()`.

> **Open question — verify during implementation.** `SoundMixer` is currently instantiated inside `MixerViewModel` (not shared at `App.kt` scope). The onboarding VM needs access to the SAME mixer instance so its fades affect what the user hears. Likely change: hoist the `SoundMixer` to `App.kt` (created with `remember`) and pass it into both `MixerViewModel` and `OnboardingViewModel`. Confirm by reading `MixerViewModel` constructor in step 2 of implementation order.

## 7. Navigation Integration (`App.kt`)

Changes to [App.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt):

1. **Add route:** `data object Onboarding : AppScreen` to the `sealed interface AppScreen`.
2. **Repository + splash gate.** At the top of `App()`:
   ```kotlin
   val onboardingRepository = remember { OnboardingRepository() }
   val hasCompleted by onboardingRepository.hasCompletedFlow
       .collectAsState(initial = null)
   ```
3. **Splash render:** wrap the existing `Box(modifier = Modifier.fillMaxSize())` content with:
   ```kotlin
   when (hasCompleted) {
       null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
       false -> { /* initial currentScreen = AppScreen.Onboarding */ … }
       true -> { /* initial currentScreen = AppScreen.Mixer */ … }
   }
   ```
   Practically, do this by initialising `currentScreen` from `hasCompleted` once it becomes non-null:
   ```kotlin
   var currentScreen by remember { mutableStateOf<AppScreen?>(null) }
   LaunchedEffect(hasCompleted) {
       if (currentScreen == null && hasCompleted != null) {
           currentScreen = if (hasCompleted == true) AppScreen.Mixer else AppScreen.Onboarding
       }
   }
   if (currentScreen == null) {
       Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
       return@FocusRitualTheme
   }
   ```
4. **AnimatedContent transitions:** extend the existing `transitionSpec` `when` block with the Onboarding → Mixer case:
   ```kotlin
   initial is AppScreen.Onboarding && target is AppScreen.Mixer -> {
       val enter = fadeIn(tween(800, easing = FocusRitualEasing.Atmospheric)) +
           scaleIn(tween(800, easing = FocusRitualEasing.DeepEaseOut), initialScale = 0.96f)
       val exit = fadeOut(tween(600, easing = FocusRitualEasing.Atmospheric)) +
           scaleOut(tween(600, easing = FocusRitualEasing.Atmospheric), targetScale = 1.02f)
       enter togetherWith exit
   }
   ```
   (`EaseOutCubic` from the user spec maps to the project's `FocusRitualEasing.DeepEaseOut`; if that mapping is wrong on inspection, fall back to `androidx.compose.animation.core.EaseOutCubic`.)
5. **Composable branch:** add to the `when (screen)`:
   ```kotlin
   AppScreen.Onboarding -> OnboardingScreen(
       viewModel = onboardingViewModel,
       onComplete = { currentScreen = AppScreen.Mixer },
   )
   ```

## 8. Component-by-Component Spec

All composables follow the stateless contract `(state, onAction): Unit` where applicable, accept `modifier: Modifier = Modifier` as the last parameter, and use only design-system tokens (§4). All animations use `tween` / `infiniteRepeatable` with `OrganicEasing` or `FocusRitualEasing.*` unless noted.

### `AtmosphericBackdrop(showForest: Boolean = false, particleCount: Int = 5, glowIntensity: Float = 1f, modifier: Modifier = Modifier)`
Purpose: composes all background layers in a single `Box(fillMaxSize)` painted with `colorScheme.surface`. OCP: parameters allow per-step tuning (Approach: 5 particles, no forest, glow 0.7; Inside: 6 particles, forest enabled, glow 1.0; Pillars: 3 particles, no forest, glow 0.6).
Layer stack (back-to-front):
1. `colorScheme.surface` fill
2. `ForestKenBurnsLayer` (if `showForest`)
3. `AmbientGlowPulse(intensity = glowIntensity)`
4. `DriftingMistLayer`
5. `DriftingParticleField(count = particleCount)`
6. `VignetteOverlay`
7. `FilmGrainOverlay`

### `ForestKenBurnsLayer(modifier: Modifier = Modifier)`
- Optional `Image` rendered at `Modifier.fillMaxSize().graphicsLayer { scaleX = animatedScale; scaleY = animatedScale; translationX = …; alpha = 0.32f }`.
- Infinite transition: scale `1.0f → 1.06f` over `40_000ms`, easing `OrganicEasing`, `RepeatMode.Reverse`.
- Tint via `ColorFilter.tint(colorScheme.surface, BlendMode.Multiply)` to keep it sanctuary-dark.
- **Asset note:** if no forest image asset exists yet, fall back to a vertical linear gradient (`colorScheme.surface` → `colorScheme.surfaceContainer` → `colorScheme.surface`) painted on the same animated layer. Add a TODO for the asset.

### `DriftingMistLayer(modifier: Modifier = Modifier)`
- Two `Box`es each with `background(Brush.radialGradient(colors = listOf(colorScheme.onSurface.copy(alpha = 0.05f), Color.Transparent), radius = …))`.
- Each drifts via `infiniteTransition`: translationX `-80.dp..80.dp` over `24_000ms`, translationY `-40.dp..40.dp` over `30_000ms`, both `OrganicEasing`, `Reverse`.
- `blur(28.dp)` modifier on each box. **API guard:** wrap in `Modifier.then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(28.dp) else Modifier)` — see §15.

### `AmbientGlowPulse(intensity: Float = 1f, modifier: Modifier = Modifier)`
- Single centered `Box(size = 480.dp).background(Brush.radialGradient(colors = listOf(colorScheme.primary.copy(alpha = 0.10f * intensity), Color.Transparent)))`.
- Breathing alpha animation `0.6f..1.0f` over `6000ms`, `OrganicEasing`, `Reverse`.

### `DriftingParticleField(count: Int, modifier: Modifier = Modifier)`
- `data class Particle(val seedX: Float, val seedY: Float, val sizeDp: Float, val phaseMs: Int, val driftSec: Int)`.
- Particles built once: `val particles = remember(count) { List(count) { Particle(seedX = Random.nextFloat(), seedY = Random.nextFloat(), sizeDp = Random.nextFloat() * 2f + 1.5f, phaseMs = Random.nextInt(0, 6000), driftSec = Random.nextInt(18, 28)) } }` — seed stable across recompositions.
- Each particle is a `Box(size = particle.sizeDp.dp).graphicsLayer { translationX/Y = animated; alpha = animated }.background(colorScheme.onSurface.copy(alpha = 0.45f), CircleShape).blur(0.3.dp)` (blur guarded — see §15).
- Animation: translationY drifts upward `0.dp..-120.dp`, translationX wobbles `-20.dp..20.dp`, alpha pulses `0.0f..0.45f..0.0f`. Period = `driftSec * 1000ms`. Each particle's `infiniteTransition` is offset by `phaseMs` via `StartOffset(phaseMs, StartOffsetType.Delay)`.

### `VignetteOverlay(modifier: Modifier = Modifier)`
- `Box(fillMaxSize).background(Brush.radialGradient(colors = listOf(Color.Transparent at 0.4f stop, colorScheme.surface.copy(alpha = 0.85f) at 1.0f stop)))`.

### `FilmGrainOverlay(modifier: Modifier = Modifier)`
- Static `Canvas` painting a sparse pattern of tiny `colorScheme.onSurface.copy(alpha = 0.04f)` dots OR a low-alpha `ShaderBrush` built from a tiled noise pattern. **Not animated.** Renders once per composition.

### `BreathingOrb(modifier: Modifier = Modifier, size: Dp = 240.dp)`
- Concentric layers (back-to-front):
  1. Outer halo: `Box(size = size * 1.6f).background(Brush.radialGradient(colors = listOf(colorScheme.primary.copy(alpha = 0.20f), Color.Transparent)))` — alpha breathes `0.16f..0.30f` over `7200ms` (`OrganicEasing`, `Reverse`).
  2. Mid glow: `Box(size = size * 1.15f).background(Brush.radialGradient(colors = listOf(colorScheme.primary.copy(alpha = 0.40f), Color.Transparent)))` — scale `0.98f..1.02f` over `7200ms`.
  3. Core: `Box(size = size).clip(CircleShape).background(Brush.radialGradient(colors = listOf(colorScheme.onSurface.copy(alpha = 0.92f), colorScheme.onSurface.copy(alpha = 0.70f))))` — scale `0.97f..1.03f` over `7200ms`, `OrganicEasing`, `Reverse`.
- Uses `OrganicEasing` from `core.designsystem.theme`. Does NOT depend on or affect the timer orb.

### `AnimatedFadeUp(delayMs: Int = 0, durationMs: Int = 800, translationDp: Dp = 16.dp, content: @Composable () -> Unit)`
- `var visible by remember { mutableStateOf(false) }; LaunchedEffect(Unit) { delay(delayMs); visible = true }`.
- `AnimatedVisibility(visible, enter = fadeIn(tween(durationMs, easing = FocusRitualEasing.Atmospheric)) + slideInVertically(tween(durationMs, easing = FocusRitualEasing.Atmospheric)) { translationDp.roundToPx() }) { content() }`.

### `AnimatedFadeIn(delayMs: Int = 0, durationMs: Int = 800, content: @Composable () -> Unit)`
- As above but fade-only.

### `PulsingTapHint(text: String, modifier: Modifier = Modifier)`
- Centered `Text(text, fontWeight = FontWeight.W300, color = colorScheme.onSurface.copy(alpha = 0.45f))`.
- Alpha pulses `0.32f..0.60f` over `2400ms`, `OrganicEasing`, `Reverse`.

### `StepIndicator(current: OnboardingStep, modifier: Modifier = Modifier)`
- `Row(horizontalArrangement = Arrangement.spacedBy(8.dp))` of 3 indicators.
- Inactive: `Box(size = 6.dp).background(colorScheme.onSurface.copy(alpha = 0.20f), CircleShape)`.
- Active: `Box(size = DpSize(width = 18.dp, height = 6.dp)).background(colorScheme.onSurface.copy(alpha = 0.70f), RoundedCornerShape(3.dp))`.
- `animateDpAsState` for width transitions (`tween(420, FocusRitualEasing.Atmospheric)`).

### `ShimmerPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier)`
- `Box(modifier.height(56.dp).clip(RoundedCornerShape(28.dp)).background(colorScheme.onSurface.copy(alpha = 0.08f)).border(1.dp, colorScheme.onSurface.copy(alpha = 0.16f), RoundedCornerShape(28.dp)).clickable(indication = null, interactionSource = …) { onClick() })`.
- Overlay: animated horizontal sweep `Brush.horizontalGradient(colors = listOf(Color.Transparent, colorScheme.onSurface.copy(alpha = 0.10f), Color.Transparent), startX = animatedX, endX = animatedX + 200.dp.toPx())` cycling every `3200ms`.
- Press feedback: `scale(0.97f)` via `animateFloatAsState` on `isPressed`. No ripple.
- Centered `Text(text, fontWeight = FontWeight.W300, color = colorScheme.onSurface.copy(alpha = 0.88f), letterSpacing = 0.5.sp)`.

### `PillarCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier)`
- `Column(modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colorScheme.surfaceContainer).border(1.dp, colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp))`.
- Row: `Icon(icon, contentDescription = null, tint = colorScheme.onSurface.copy(alpha = 0.70f))` + `Text(title, fontWeight = W400, color = colorScheme.onSurface.copy(alpha = 0.85f))`.
- Subtitle: `Text(subtitle, fontWeight = W300, color = colorScheme.onSurface.copy(alpha = 0.45f), lineHeight = 1.4.em)`.

## 9. Screen Specs

All three steps render inside `OnboardingScreenContent`'s `AnimatedContent` (see §12). They share `AtmosphericBackdrop` (animated by its own parameters per step).

### StepApproach (`OnboardingStep.Approach`)

Modifier: `Modifier.fillMaxSize().clickable(indication = null, interactionSource = …) { onIntent(AdvanceStep) }`.

Layout (Box, `Alignment.Center` for primary column):
- `AtmosphericBackdrop(showForest = false, particleCount = 5, glowIntensity = 0.7f)`
- Centered Column:
  - `AnimatedFadeIn(delayMs = 600, durationMs = 1600)`: wordmark `Text("FOCUSRITUAL", fontWeight = W200, letterSpacing = 8.sp, color = colorScheme.onSurface.copy(alpha = 0.85f))`.
  - `Spacer(24.dp)`.
  - `AnimatedFadeUp(delayMs = 1800, durationMs = 1200)`: tagline `Text("Step into the sanctuary", fontWeight = W300, color = colorScheme.onSurface.copy(alpha = 0.45f))`.
- Bottom-anchored Column (`Alignment.BottomCenter`, `padding(bottom = 48.dp)`):
  - `AnimatedFadeIn(delayMs = 3200)`: `PulsingTapHint(text = "Tap anywhere to begin")`.
  - `Spacer(24.dp)`.
  - `StepIndicator(current = Approach)`.

### StepInside (`OnboardingStep.Inside`)

Modifier: same tap-to-advance clickable wrapping the whole screen.

Layout:
- `AtmosphericBackdrop(showForest = true, particleCount = 6, glowIntensity = 1.0f)`
- Centered Column:
  - `AnimatedFadeIn(delayMs = 300, durationMs = 1400)`: `BreathingOrb(size = 200.dp)`.
  - `Spacer(40.dp)`.
  - `AnimatedFadeUp(delayMs = 1100, durationMs = 1000)`: headline `Text("You are inside.", fontWeight = W300, color = colorScheme.onSurface.copy(alpha = 0.88f))`.
  - `Spacer(12.dp)`.
  - `AnimatedFadeUp(delayMs = 1500, durationMs = 1000)`: body `Text("Breathe with the forest.\nThe noise of the day stays outside.", textAlign = Center, fontWeight = W300, color = colorScheme.onSurface.copy(alpha = 0.60f), lineHeight = 1.6.em)`.
- Bottom-anchored:
  - `AnimatedFadeIn(delayMs = 2800)`: `PulsingTapHint("Tap to continue")`.
  - `StepIndicator(current = Inside)`.

### StepPillars (`OnboardingStep.Pillars`)

Modifier: NOT click-to-advance — only the CTA advances.

Layout:
- `AtmosphericBackdrop(showForest = false, particleCount = 3, glowIntensity = 0.6f)`
- `Column(fillMaxSize, padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(20.dp))`:
  - `Spacer(weight = 1f)` (or equivalent vertical padding `top = 80.dp`).
  - `AnimatedFadeUp(delayMs = 200)`: title `Text("Three pillars hold the sanctuary.", fontWeight = W300, fontSize = 22.sp, color = colorScheme.onSurface.copy(alpha = 0.88f), textAlign = Center)`.
  - `Spacer(32.dp)`.
  - `AnimatedFadeUp(delayMs = 600)`: `PillarCard(icon = Icons.Outlined.GraphicEq, title = "Mix", subtitle = "Layer rain, wind, fire, and field into your own ambient cathedral.")`.
  - `AnimatedFadeUp(delayMs = 900)`: `PillarCard(icon = Icons.Outlined.Timer, title = "Focus", subtitle = "Breathe through deliberate sessions. The orb keeps your rhythm.")`.
  - `AnimatedFadeUp(delayMs = 1200)`: `PillarCard(icon = Icons.Outlined.Shield, title = "Protect", subtitle = "Block distractions at the system level so the work can land.")`.
  - `Spacer(weight = 1f)`.
  - `AnimatedFadeUp(delayMs = 1800)`: `ShimmerPillButton(text = "Enter the Sanctuary", onClick = { onIntent(CompleteOnboarding) })`.
  - `Spacer(20.dp)`.
  - `StepIndicator(current = Pillars)`.
  - `Spacer(40.dp)`.

> Icon picks (`Icons.Outlined.GraphicEq`, `Timer`, `Shield`) are placeholders — confirm during implementation against `Icons.Outlined.*` set; substitute closest match if absent.

## 10. Audio Integration

### Reality check (from orientation)
`SoundMixer` has NO `fadeIn` / `setVolumeAnimated` method — verified at [composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt). Its only public surface for volume is `syncState(commands: List<AudioCommand>)`. Per-sound volume is driven by emitting `AudioCommand(id, enabled, volume)` snapshots.

**Therefore fade-in is implemented at the VM level**, not in `SoundMixer`. The onboarding VM runs a small coroutine per fade:

```kotlin
private suspend fun fadeInSound(soundId: String, target: Float, durationMs: Int) {
    val anim = Animatable(0f)
    anim.animateTo(
        targetValue = target,
        animationSpec = tween(durationMs, easing = FocusRitualEasing.Atmospheric),
    ) {
        soundMixer.syncState(listOf(AudioCommand(id = soundId, enabled = true, volume = value)))
    }
}
```

(Exact `AudioCommand` field names + `SoundId` constants TBD — verify against `feature/mixer/domain/MixRepository.kt` and `core/audio/`. Use the `"rain"` / `"wind"` string IDs already present at [MixRepository.kt#L44-L45](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixRepository.kt#L44).)

### VM call sites
- `init { viewModelScope.launch { fadeInSound("wind", target = 0.18f, durationMs = 2000) } }`
- On `AdvanceStep` transitioning to `Inside`: `viewModelScope.launch { fadeInSound("rain", target = 0.30f, durationMs = 1500) }`
- On `CompleteOnboarding`, BEFORE invoking `onComplete`:
  1. Persist `{rain: enabled, volume 0.30; wind: enabled, volume 0.18; everything else: as-is}` to `AmbientStateRepository` (use existing setter API — see [AmbientStateRepository.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/data/AmbientStateRepository.kt)).
  2. `repository.markCompleted()`.
  3. Invoke `onComplete` callback (causes `App.kt` to switch to Mixer; `MixerViewModel` reads `AmbientStateRepository` on init and inherits the faded values rather than calling `MixRepository` defaults).

### Default-startup gating
`MixerViewModel` / `MixRepository` currently apply hard-coded defaults (rain 0.7 enabled, wind 0.5 enabled — see [MixRepository.kt#L44-L45](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixRepository.kt#L44)). These must be gated:
- Inject `OnboardingRepository` into whichever entity performs the "first-run default" logic (likely `AmbientStateRepository` initialization OR `MixerViewModel.init`).
- If `hasCompleted == false` AND no persisted ambient state exists, write a blank/silent baseline (`rain: disabled, volume 0`, `wind: disabled, volume 0`) so the Onboarding VM's `syncState` calls are the first audible events.
- If `hasCompleted == true` AND persisted state exists, use that.
- If `hasCompleted == true` AND no persisted state (edge case — e.g. user cleared mix data after onboarding), apply the existing defaults.

> **Verify during implementation.** Read `MixerViewModel.init` and `AmbientStateRepository` constructor to pinpoint exactly where the defaults are seeded; that's the single insertion point for the gating check.

### Shared `SoundMixer` instance
As noted in §6, the onboarding VM and the mixer VM must talk to the same `SoundMixer`. Likely refactor: hoist `SoundMixer` construction into `App.kt` as `remember { SoundMixer() }` and pass to both VMs. Document this as part of the implementation order (step 2).

## 11. Haptics

- On `AdvanceStep`: `hapticController.perform(HapticFeedbackType.Light)` (called at the start of `onIntent`, before state transition).
- On `CompleteOnboarding`: `hapticController.perform(HapticFeedbackType.Medium)`.
- No special gating logic — `HapticController` already respects `HapticSettingsRepository` internally.

## 12. Step Transitions

Inside `OnboardingScreenContent`:

```kotlin
AnimatedContent(
    targetState = state.step,
    transitionSpec = {
        (fadeIn(tween(900, easing = FocusRitualEasing.Atmospheric)) +
         scaleIn(tween(900, easing = FocusRitualEasing.Atmospheric), initialScale = 0.98f))
        .togetherWith(
            fadeOut(tween(700, easing = FocusRitualEasing.Atmospheric)) +
            scaleOut(tween(700, easing = FocusRitualEasing.Atmospheric), targetScale = 1.02f)
        ).using(SizeTransform(clip = false))
    },
    label = "OnboardingStepTransition",
) { step -> when (step) { Approach -> StepApproach(...); Inside -> StepInside(...); Pillars -> StepPillars(...) } }
```

Note: `AtmosphericBackdrop` could live OUTSIDE the `AnimatedContent` to provide continuous backdrop continuity across steps, with only the step-specific foreground crossfading. Either approach is valid — preferred placement is OUTSIDE (continuous backdrop), with per-step parameters animated via `animateFloatAsState` / `animate*AsState` for `glowIntensity` and `particleCount` (latter requires a more careful approach — either keep particle count constant at 6 and just vary alpha, or accept the small visual blip).

## 13. OrganicEasing — No Move Required

Already shared at [core/designsystem/theme/Motion.kt#L23](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt#L23):
```kotlin
val OrganicEasing = CubicBezierEasing(0.3f, 0.0f, 0.15f, 1.0f)
```
Active references:
- [feature/timer/ui/AtmosphericField.kt#L29](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/AtmosphericField.kt#L29) — already imports from `core.designsystem.theme`.
- [feature/mixer/ui/HeroSessionButton.kt#L34](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/HeroSessionButton.kt#L34)
- [core/designsystem/component/PlayButton.kt#L35](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt#L35)

Onboarding components add a fourth importer via `import com.focusritual.app.core.designsystem.theme.OrganicEasing`. No file moves; the user spec's instruction to move it is superseded by current codebase state. (Update the user-confirmed decisions list accordingly if anyone re-reads the original spec.)

## 14. SOLID Notes

- **SRP** — each backdrop layer is its own composable file; each does one visual effect. `BreathingOrb`, `ShimmerPillButton`, `PillarCard`, `StepIndicator` are single-purpose.
- **OCP** — `AtmosphericBackdrop` is parameterised (`showForest`, `particleCount`, `glowIntensity`) so new steps can re-use it without modification. `AnimatedFadeUp` / `AnimatedFadeIn` are reusable entrance utilities.
- **DIP** — `OnboardingViewModel` depends on abstractions (`OnboardingRepository`, `SoundMixer`, `HapticController`, `AmbientStateRepository`) injected via constructor.
- **LSP** — all three steps are `@Composable (state, onIntent) -> Unit` shaped; `AnimatedContent` transitions are uniform, so adding a 4th step is purely additive.
- **ISP** — `OnboardingIntent` is a sealed interface with exactly the two intents needed; no god-intent.

## 15. Performance / Risk Notes

- **Compose-tree animation load.** Backdrop hosts: forest (1 animator), mist (2 boxes × 2 animators = 4), glow (1), particles (5–6 × ~3 animators = up to 18), step-content entrance animators, breathing orb (3). Worst case ~30 concurrent animations on StepInside. Acceptable on iOS 18+ and modern Android, but: ① keep all animations `infiniteRepeatable` with `tween` (cheap), no `Canvas` per-frame work, ② consider `derivedStateOf` to avoid recomposition cascades, ③ the film-grain overlay must be drawn ONCE (not animated) — flag during code review.
- **`Modifier.blur` API requirement.** Compose `blur` requires API 31+ on Android. Project `android-minSdk = 28` (verified at [gradle/libs.versions.toml#L4](gradle/libs.versions.toml#L4)). Two options: (a) raise minSdk to 31 — likely undesirable; (b) provide a per-platform `expect fun Modifier.softBlur(radius: Dp): Modifier` with iOS = real blur, Android API ≥ 31 = real blur, Android API < 31 = no-op (just return `this`). **Recommended: option (b)**, implemented as a small expect/actual under `core/designsystem/`. Plan calls for this helper to be added as part of Step 1.
- **`blur(0.3.dp)` on particles** — same API guard.
- **Film grain** — DO NOT animate (would crater frame rate). Use a single `ShaderBrush` or pre-baked `BitmapPainter`.
- **First-frame priority** — render `AtmosphericBackdrop` immediately so there's no black flash after the splash gate releases. The wordmark/orb entrance is delayed (600ms+) on purpose.
- **Recomposition stability** — `Particle` instances live inside `remember(count)` so seeds don't regenerate. `OnboardingUiState` is `data class` (stable).

## 16. Acceptance Criteria

Visual & behavioural (from the user spec):
- [ ] On a clean install, app boots into Onboarding (after at most one frame of splash black).
- [ ] StepApproach: wordmark fades in by 2.2s; tagline fades up by 3s; tap hint pulses by 3.2s.
- [ ] Tap anywhere on StepApproach advances to StepInside with the documented `AnimatedContent` transition (~900ms).
- [ ] StepInside: BreathingOrb visible and breathing; forest backdrop active; rain audibly faded in over 1.5s starting on entry.
- [ ] Wind audibly faded in on StepApproach over 2s starting on first appearance.
- [ ] StepPillars: three cards appear in staggered sequence; CTA appears last; CTA shows shimmer sweep.
- [ ] Tap CTA → onboarding transitions to Mixer (~800ms enter, ~600ms exit) and Mixer plays with Rain ≈ 0.30 and Wind ≈ 0.18 (NOT 0.70/0.50 defaults).
- [ ] On second launch (or after any subsequent kill), app boots directly into Mixer; no onboarding rendered.
- [ ] No `Color.White`, no `Color.Black`, no `Color(0xFF…)` literal in any new file (grep should return zero hits).
- [ ] All colors flow through `MaterialTheme.colorScheme.*`.
- [ ] Every clickable in onboarding uses `indication = null` and a remembered `MutableInteractionSource`.
- [ ] Fonts: only `W200` (wordmark), `W300`, `W400` used.
- [ ] Light haptic on each tap-to-advance; medium haptic on CTA.

Build & regression:
- [ ] `./gradlew :composeApp:compileDebugKotlinAndroid` passes.
- [ ] `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` passes.
- [ ] Existing Mixer / FocusSession / ActiveSession flows still work end-to-end (smoke test — start a session, finish, return to Mixer).
- [ ] Timer screen still works after touching anywhere near `OrganicEasing` usage (no change planned, but verify).
- [ ] No new lint warnings about ripple-less clickables (project already uses this pattern).

## 17. Implementation Order

1. **Pre-work** — add `expect fun Modifier.softBlur(radius: Dp): Modifier` under `core/designsystem/` with iosMain (always real `blur`) and androidMain (API-gated). (`OrganicEasing` move is NOT needed — already shared.)
2. **Shared `SoundMixer`** — hoist `SoundMixer` instantiation into `App.kt`; update `MixerViewModel` constructor to accept it (instead of creating its own). Confirm Mixer still functions. This unblocks step 4.
3. **`OnboardingRepository`** + JsonStore key — write & read `hasCompleted`. Wire `remember { OnboardingRepository() }` in `App.kt`.
4. **`OnboardingContract`** + **`OnboardingViewModel`** with audio fade methods (string IDs `"rain"` / `"wind"` per [MixRepository.kt#L44-L45](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixRepository.kt#L44)).
5. **Backdrop layers** (bottom-up): `FilmGrainOverlay` → `VignetteOverlay` → `AmbientGlowPulse` → `DriftingMistLayer` → `ForestKenBurnsLayer` → `DriftingParticleField` → `AtmosphericBackdrop`.
6. **Foreground primitives**: `AnimatedFadeIn`, `AnimatedFadeUp`, `PulsingTapHint`, `StepIndicator`, `BreathingOrb`, `ShimmerPillButton`, `PillarCard`.
7. **Step composables**: `StepApproach`, `StepInside`, `StepPillars`.
8. **`OnboardingScreen` + `OnboardingScreenContent`** — stateful/stateless split; backdrop hoisted above `AnimatedContent` for continuity.
9. **`App.kt` integration** — `Onboarding` route, splash gate, transition spec, composable branch.
10. **Audio default-startup gating** — modify `AmbientStateRepository` (or `MixerViewModel.init`) to respect `OnboardingRepository.hasCompleted`; implement ambient handoff in `CompleteOnboarding`.
11. **Build verify** — `compileDebugKotlinAndroid` + `linkDebugFrameworkIosSimulatorArm64`. Fix until green.
12. **Manual visual check** — iOS Simulator (iPhone 16 / iOS 18) + an Android emulator with API ≥ 31 (to validate blur) AND one with API 28 (to validate `softBlur` no-op fallback).

## 18. Open Questions / Verify During Implementation

| # | Item | Status |
|---|---|---|
| 1 | `SoundMixer` fade API | **No native fade.** Use `Animatable` + `syncState(listOf(AudioCommand(...)))` per §10. |
| 2 | DI wiring location for VM + Repository | `App.kt` `viewModel { … }` blocks, alongside existing `mixerViewModel` / `settingsViewModel`. Also requires hoisting `SoundMixer` (step 2). |
| 3 | Android minSdk vs `blur` modifier | minSdk = 28 ([gradle/libs.versions.toml#L4](gradle/libs.versions.toml#L4)); `blur` needs 31+. Mitigated via `expect/actual softBlur` helper (step 1). |
| 4 | Material Icons Extended | **Already present** at [composeApp/build.gradle.kts#L38](composeApp/build.gradle.kts#L38). No action. |
| 5 | `OrganicEasing` location | **Already shared** at [core/designsystem/theme/Motion.kt#L23](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt#L23). No move. |
| 6 | Exact `AudioCommand` field names + how `MixerViewModel` calls `syncState` | Verify in [core/audio/SoundMixer.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt) + `MixerViewModel` during step 4. |
| 7 | `AmbientStateRepository` setter API for writing the faded-in mix | Read during step 10; if no high-level "set full state" setter exists, add one or call per-sound setters. |
| 8 | `MixerViewModel` constructor — does it currently instantiate `SoundMixer` internally? | Confirm during step 2; refactor if so. |
| 9 | Icon picks (`Icons.Outlined.GraphicEq` / `Timer` / `Shield`) | Confirm existence in Material Icons Extended during step 6; substitute closest match if absent. |
| 10 | `FocusRitualEasing.DeepEaseOut` vs `EaseOutCubic` mapping | Use `DeepEaseOut`; if visually wrong on inspection, swap to `EaseOutCubic`. |

---

**End of plan.**
