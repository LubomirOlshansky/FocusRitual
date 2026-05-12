# Mixer Orb Animation — Plan

## Summary
Short plan to adapt the Mixer "hero orb" into a FocusRitual-style session orb used on the Mixer screen only. This plan targets the mixer hero button (orb) and does NOT modify session setup or timer files. The orb logic must use the existing boolean audio-playing flag (`MixerUiState.isPlaying`) and must not introduce or depend on any per-sound amplitude fields.

## Evidence & References
- Mixer entry point and usage: [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L1-L320)
- Current hero orb implementation: [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/HeroSessionButton.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/HeroSessionButton.kt#L1-L220)
- Mixer UI state shape (audio playing flag): [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt#L1-L80)
- MixerViewModel orchestration & live offsets: [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L1-L240)
- Design system guidance (tokens, spacing, typography, animation rules): [docs/design-system.md](docs/design-system.md#L1-L400)

## Questions & Answers
1. What exact file(s) should implementation modify?
- Primary: `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/HeroSessionButton.kt` — replace or refactor the current `HeroSessionButton` body to implement the new orb behavior while preserving the same public API (signature). This avoids touching `MixerScreen.kt`.
- Optional: add a new file in the same package `feature/mixer/ui/SessionOrb.kt` and implement `HeroSessionButton` to delegate to it. Either approach is acceptable; preferred minimal-change approach is editing `HeroSessionButton.kt` in-place.

2. What is the existing audio-playing state field name to wire into `SessionOrb`?
- `MixerUiState.isPlaying` (boolean). See `MixerContract.MixerUiState` and usages in `MixerScreen.kt`.

3. Where should `SessionOrb` be placed to preserve the same position and size footprint?
- Keep the composable where `HeroSessionButton` is currently used (center of the hero column in `MixerScreenContent`). Implement the orb at the same default size used today (the existing `HeroSessionButton` uses `Modifier.size(150.dp)`), keeping that footprint and the same call-site signature (`isPlaying: Boolean, onStartSession: () -> Unit, modifier: Modifier = Modifier`).

4. What imports or Compose APIs will be needed?
- Animation primitives: `rememberInfiniteTransition`, `animateFloat`, `animateFloatAsState`, `infiniteRepeatable`, `RepeatMode`, `tween`, `spring` (for organic motion fallback), `animate*AsState` variants.
- Interaction: `MutableInteractionSource`, `collectIsPressedAsState` for press-scale.
- Drawing APIs: `Modifier.drawBehind`, `Brush.radialGradient`, `drawCircle`, `graphicsLayer` (scale), `shadow`, `clip`, `background`, `border`.
- Material tokens: `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*` and `Spacing` constants from the design system where appropriate.
- Kotlin utilities: `remember`, `derivedStateOf` / `remember { }` for local state and derived animation inputs based on `MixerUiState.isPlaying`.

5. Are there any design-system conflicts in the user-provided snippet (e.g., raw colors like `Color(0xFF161E2A`)?
- Current `HeroSessionButton.kt` follows the design system tokens (uses `surfaceBright`, `outlineVariant`, `onSurface`, `scrim`) and `GlowColor` from the design system; there are no raw hex tokens in the current hero file.
- Rule: Any new orb implementation must avoid hard-coded hex/colors (no `Color(0xFF...)` or `Color.White`) and must use `MaterialTheme.colorScheme.*` and `Spacing` constants from `core/designsystem`. Use `GlowColor` and other design tokens from `core/designsystem/theme` when appropriate.
- Also avoid adding ripples — use `indication = null` and `MutableInteractionSource` for press handling.

6. What exact verification commands should Developer run later?
- Android debug APK assemble: `./gradlew :composeApp:assembleDebug`
- iOS framework link for simulator (if building iOS host): `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- Optional manual verification: run the app on Android emulator / iOS simulator and visually confirm the orb behavior in the Mixer screen.

7. What files are explicitly out of bounds?
- Do NOT modify: `StartSessionButton.kt`, `FocusSessionScreen.kt`, `FocusSessionViewModel.kt`, `App.kt`, or any other session configuration / setup surfaces under `feature/session/*` and `feature/timer/*`. Keep LiveActivity, session setup, and timer UI untouched.

## Concrete Implementation Plan (high level)
Goal: Replace the static circle label with an animated session orb that:
- Uses only the existing boolean `MixerUiState.isPlaying` to drive visual mode (stopped vs playing).
- Keeps existing press-scale behavior.
- Uses `BEGIN\nSESSION` (two-line label), no subtitle/pill.
- Uses design-system tokens (no raw colors) and existing size/placement.

Tasks
1. Implement `SessionOrb` behavior driven solely by the boolean `MixerUiState.isPlaying`:
   - Decide ring count, durations, glow and label alpha based on `isPlaying` (see Acceptance criteria table below).
   - Use `derivedStateOf` or simple `if` branches to switch animation parameters between stopped/playing modes.
2. Implement breathing rings using `rememberInfiniteTransition` and animate their radii/alpha per the parameters selected for `isPlaying`.
   - Base radii should be centered inside the existing `Modifier.size(150.dp)` footprint; the nested sizes (110/126dp) may be used proportionally or centered inside that footprint to preserve visual balance.
3. Preserve and reuse existing glow/inner core drawing and shadow from current `HeroSessionButton`.
4. Replace the label text with `"BEGIN\nSESSION"` (two-line). Use `MaterialTheme.typography.titleLarge` with `FontWeight.SemiBold` and `letterSpacing` consistent with existing button rules. Center text and keep uppercase.
5. Keep clickable behavior and `MutableInteractionSource` with `indication = null`; keep `pressScale` animation as is.
6. Ensure all color references use `MaterialTheme.colorScheme.*` or `GlowColor` from design system. Where alpha overlays are needed use `.copy(alpha = ...)` on tokens.
7. Add small performance guard: rely only on `isPlaying` boolean; when `isPlaying` is false show stopped-mode subtle animation, when true switch to playing-mode animation. No per-sound data or amplitude aggregation is used.

Acceptance criteria
 - Visual: Orb renders in the same place and size as before (no layout shift).
 - Motion: Rings animate continuously and subtly; ring count/duration/glow/label alpha follow the boolean-driven table in Acceptance criteria.
 - Press: On press, orb scales down slightly and returns, matching existing timing (`tween(150)`~`tween(200)`).
 - Label: The orb shows `BEGIN` on top line and `SESSION` below (centered), using `titleLarge` + `SemiBold`, uppercase.
 - No subtitle, no additional pill button, no text like "tap when ready".
 - Design tokens: No hard-coded hex colors; all colors pulled from `MaterialTheme.colorScheme.*` or sanctioned design tokens (e.g., `GlowColor`).
 - Build: App builds with `./gradlew :composeApp:assembleDebug` and iOS linking step (if used) `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.

## Verification steps
1. Run `./gradlew :composeApp:assembleDebug` and install APK on emulator / device.
2. Observe Mixer screen hero orb:
   - Toggle audio play state in the mixer and verify orb switches between stopped and playing modes per Acceptance criteria.
   - Tap and hold the orb to verify press-scale animation.
   - Confirm label text reads exactly `BEGIN\nSESSION`, centered, and no subtitle or pill.
3. (iOS) Run `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` and run the host iOS app; verify behavior in the iOS simulator.

## Risks & Notes (adaptations)
 - Performance: drawing large radial gradients and 3 animated rings can be GPU-heavy on low-end devices; cap ring sizes and reduce alpha complexity if frame drops are observed.
 - Styling differences: existing design system suggests a 96dp core — current project uses `150.dp`. Preserve the current size (`Modifier.size(150.dp)`) to avoid layout changes; if design asks to standardize later, move to `Spacing` tokens.
 - Spacing & padding: use `Spacing.*` tokens for padding (avoid ad-hoc mixed `Modifier.padding(horizontal = ..., bottom = ...)` patterns when introducing new paddings).
 - Out-of-scope: do NOT add per-sound amplitude aggregation, do NOT add or depend on `uiState.sounds[]` or `liveVolume`. The orb must use only `MixerUiState.isPlaying` for behavior selection.

---

If you want, I can now produce a patch that implements the `SessionOrb` composable in `HeroSessionButton.kt` (keeping the public API), or add an internal `SessionOrb(isAudioPlaying: Boolean, ...)` in the same package and delegate `HeroSessionButton` to it. This will preserve the public API shape such as `HeroSessionButton(isPlaying = uiState.isPlaying, onStartSession = ..., modifier = ...)`.

### Acceptance Criteria (exact values)
 - Stopped mode (isPlaying = false):
    - Rings: 2 rings
    - Base cycle duration: 5000 ms
    - Core glow alpha (or intensity): 0.03f
    - Label alpha: 0.48f
 - Playing mode (isPlaying = true):
    - Rings: 3 rings
    - Base cycle duration: 2800 ms
    - Core glow alpha (or intensity): 0.10f
    - Label alpha: 0.68f
 - Press interaction: only scale, border, and glow are affected on press.

### API & Layout constraints
 - Preserve call-site and footprint: keep `Modifier.size(150.dp)` for the component surface. Nested inner sized elements may use 110dp/126dp proportions or be centered inside the 150dp footprint to match current layout.
 - Preserve public API when possible: `HeroSessionButton(isPlaying: Boolean, onStartSession: () -> Unit, modifier: Modifier = Modifier)`; alternatively add an internal `SessionOrb(isAudioPlaying: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)` and delegate from `HeroSessionButton`.

### Design-system adaptations
 - Replace any raw hex colors (e.g., `Color(0xFF161E2A)`) with `MaterialTheme.colorScheme.surface`, `surfaceBright`, `scrim`, or other sanctioned tokens. Do not use `Color.White` or `Color.Black`.
 - Use `indication = null` and `MutableInteractionSource` to avoid ripple; use press-scale for tactile feedback.

If you'd like, I can implement the composable now following these constraints and run a focused build for `:composeApp:assembleDebug` to verify compilation.
