# FocusRitual — Tech Debt

## Architecture / Structure
Structure refactor (plans/structure-refactor.md) completed April 2026. **Layer invariant now enforced:** `core/` ⊄ `feature/`; cross-feature glue lives in `app/integration/`. Canonical feature shape applied to all features. See `project_structure` + `style_and_conventions` memories.

### Remaining
- [ ] **No dependency injection** — ViewModels constructed inline with default-arg constructor chains (see `MixerViewModel`). Koin was deferred (Phase 6 of mixer-refactor plan). Worth revisiting when controller/repo count grows or ViewModel wiring exceeds ~5 dependencies.
- [ ] **No navigation library** — State-based routing in `App.kt` works for 4 screens but will become unwieldy with more. Consider Voyager or Decompose if screen count grows.
- [ ] **`feature/timer/AtmosphericField.kt` has raw hex literals** — `SurfaceContainer`, `OutlineVariant`, `Primary` at file scope. Analogous to the mixer Phase 3 design-system cleanup. Should migrate to `MaterialTheme.colorScheme` or `core/designsystem/theme/Color.kt` tokens.
- [ ] **`FocusSessionScreen.kt` is 432 LOC with 4 file-private sub-composables** — candidate for `feature/session/ui/` extraction once it grows further (pattern already applied to mixer + timer).
- [ ] **`SessionPreferences.kt` is a stub** — wraps an unspecified `Settings()` type. Needs `multiplatform-settings` dependency + real impl when preset persistence is prioritised.
- [ ] **Possible module split** — composeApp is still one module. If feature boundaries stay clean, extracting `:core:audio`, `:core:designsystem`, `:feature:mixer`, etc. becomes plausible. Not urgent.

## Protect Focus / Screen Time

### P0 — Blocking before production
- [ ] **FamilyControls entitlement**: Must request from Apple Developer portal. Without it, AuthorizationCenter crashes at runtime. Requires paid developer account ($99/yr).
- [ ] **Add ScreenTimeManager.swift to Xcode target**: File is created but must be manually added to the iosApp target in Xcode project navigator.
- [ ] **Enable Family Controls capability**: Xcode → iosApp target → Signing & Capabilities → + Family Controls.
- [ ] **Physical device only**: FamilyControls does not work in iOS Simulator. Needs real device testing.

### P1 — Before feature is user-ready
- [ ] **Debug stub for Simulator testing**: Add a fake ScreenTimeHandler implementation that simulates auth + picker so the full state machine (CTA disable, sheet dismiss, state transitions) can be verified without entitlement. Swap via a boolean flag.
- [ ] **Persist FamilyActivitySelection**: Currently selection is in-memory only (`@State` in SwiftUI). For production, serialize it (conforms to `Codable`) and store in UserDefaults or a file. Selection is lost on app restart.
- [ ] **Pass selection back to Kotlin**: The Swift picker captures the selection but doesn't send it to Kotlin yet. Need a bridge method to pass serialized selection data or at least a "has blocked apps" boolean.
- [ ] **Configured/active state on ProtectFocusCard**: Currently only shows the "not configured" entry row. Need a second visual state showing shield active + count of blocked apps.
- [ ] **PermissionDenied UX**: When permission is denied, sheet stays open but no feedback is shown to the user. Add a subtle inline message or guide to Settings.

### P2 — Polish
- [ ] **Picker dismiss animation overlap**: When native picker dismisses and Compose sheet also closes, both animations may briefly overlap. Acceptable for v1 but could add a small delay.
- [ ] **Minimum iOS version**: FamilyControls requires iOS 16.0. Verify the project's minimum deployment target matches (currently iOS 18+ per ActivityKit requirement — OK).
- [ ] **ProtectFocusSetupSheet drag handle alpha**: Was changed from 0.15f → 0.12f during visual refinement but the memory in `design_system` doesn't document this.

## Audio
- [ ] **Temp file cleanup (Android)**: `AudioPlayer.android.kt` creates temp files for playback. No cleanup mechanism on app exit unless the OS handles it.
- [ ] **No `MixerViewModel` construction test** — an `UninitializedPropertyAccessException` (lateinit `organicEngine`) slipped into runtime because tests cover mappers/use-cases/audio lifecycle but not VM instantiation. A trivial `MixerViewModelInitTest { MixerViewModel() }` would catch this class of bug. Apply pattern to all ViewModels.

## Resolved (historical, kept for context)
- ~~`core/` imports `feature/`~~ → fixed in structure-refactor Phase A+B
- ~~Mixer `model/` / `modal/` / `domain/audio/` micro-folders~~ → flattened in Phase C
- ~~Timer 1527-LOC flat folder~~ → split into root + `ui/` in Phase D
- ~~Android `LiveActivityEffect.kt` missing `.android.kt` suffix~~ → renamed in Phase B
- ~~Organic engine side-effects inside `MutableStateFlow.update {}`~~ → moved to orchestrator collect/launch bodies in mixer-refactor Phase 4
- ~~`SoundIcon` enum + `toImageVector()` OCP violation~~ → icons embedded in catalog data in Phase 4
- ~~`AudioPlayer` expect-class untestable~~ → `AudioPlayerHandle` + `FakeAudioPlayerFactory` introduced in Phase 5
