# Haptic Feedback Development Plan

Add subtle, settings-gated haptic feedback at three approved ritual interaction points: enabling a sound tile, loading a saved mix, and starting a focus/sleep session.

---

## 1. Goal and Non-Goals

**Goal**
- Introduce platform haptics through a shared `core/haptic` layer that fits FocusRitual's KMP architecture.
- Persist a user-facing haptic feedback setting with `multiplatform-settings`.
- Fire haptics only for these moments:
  - Sound tile toggled ON only: light impact.
  - Saved/preset mix loaded: medium impact.
  - START SESSION tapped from session setup into active session: success notification pattern.

**Non-Goals**
- No haptics for sound tile OFF, volume changes, organic motion, remove-from-mix, save/delete mix, settings row taps, navigation, modal open/close, pause/resume, timer controls, or Live Activity actions.
- No DataStore migration.
- No dependency from `core/haptic` to `feature/settings`.
- No platform checks in `commonMain`.
- No direct platform engine calls outside `HapticController`.

---

## 2. Architecture Decisions

- Create `composeApp/src/commonMain/kotlin/com/focusritual/app/core/haptic/` as the canonical haptics layer.
- Use a common `HapticController` as the only production API used by app/features.
- Keep platform execution behind an expect/actual engine owned by `core/haptic`.
- Make `HapticSettingsRepository` a core repository using `com.russhwolf.settings.Settings`, mirroring `AudioSettingsRepository`.
- Have `feature/settings/domain/SettingsRepository` wrap `HapticSettingsRepository`; core must not import settings feature types.
- Hoist shared haptic objects in [composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt), which is already the long-lived wiring point for shared app dependencies.
- Put the Settings toggle in the **App** group, not the Audio group.
- Use success notification feedback for START SESSION because it matches the ritual transition better than a heavy impact.

Suggested common API shape:

```kotlin
enum class HapticFeedbackType {
    LightImpact,
    MediumImpact,
    Success,
}

interface HapticEngine {
    fun perform(type: HapticFeedbackType)
}

expect class PlatformHapticEngine() : HapticEngine

class HapticController(
    private val settingsRepository: HapticSettingsRepository = HapticSettingsRepository.Default,
    private val engine: HapticEngine = PlatformHapticEngine(),
) {
    fun soundTileEnabled()
    fun mixLoaded()
    fun sessionStarted()
}
```

`HapticController` should check the repository's current enabled state before calling `engine.perform(...)`. Production code outside `core/haptic` should never call `HapticEngine` or `PlatformHapticEngine` directly.

---

## 3. Files to Create or Modify

### Create

| Path | Purpose |
|---|---|
| composeApp/src/commonMain/kotlin/com/focusritual/app/core/haptic/HapticFeedbackType.kt | Shared feedback taxonomy: light impact, medium impact, success. |
| composeApp/src/commonMain/kotlin/com/focusritual/app/core/haptic/HapticEngine.kt | Common engine interface and `expect class PlatformHapticEngine`. |
| composeApp/src/commonMain/kotlin/com/focusritual/app/core/haptic/HapticController.kt | Guarded public API for approved haptic moments. |
| composeApp/src/commonMain/kotlin/com/focusritual/app/core/haptic/HapticSettingsRepository.kt | Core settings repository backed by `multiplatform-settings`. |
| composeApp/src/androidMain/kotlin/com/focusritual/app/core/haptic/AndroidHapticContext.kt | Android app context holder following the audio context pattern. |
| composeApp/src/androidMain/kotlin/com/focusritual/app/core/haptic/PlatformHapticEngine.android.kt | Android actual implementation. |
| composeApp/src/iosMain/kotlin/com/focusritual/app/core/haptic/PlatformHapticEngine.ios.kt | iOS actual implementation. |
| composeApp/src/commonTest/kotlin/com/focusritual/app/core/haptic/HapticControllerTest.kt | Verifies event mapping and settings guard. |
| composeApp/src/commonTest/kotlin/com/focusritual/app/core/haptic/HapticSettingsRepositoryTest.kt | Verifies default and persistence behavior if isolated settings test support is available. |

### Modify

| Path | Change |
|---|---|
| [composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt) | Hoist shared haptic settings/controller; pass controller to mixer/session and settings repository to settings VM if needed. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | Inject `HapticController`; fire light impact only when a sound changes from disabled to enabled; fire medium impact after valid mix load. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/FocusSessionScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/FocusSessionScreen.kt) | Inject/call `HapticController.sessionStarted()` in the `FocusSessionIntent.StartSession` transition path before navigating active. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/domain/SettingsRepository.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/domain/SettingsRepository.kt) | Wrap `HapticSettingsRepository`; expose haptics enabled flow and setter. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/SettingsContract.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/SettingsContract.kt) | Add `hapticsEnabled` to `SettingsUiState` and `SetHapticsEnabled` intent. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/SettingsViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/SettingsViewModel.kt) | Combine haptics flow into UI state and handle `SetHapticsEnabled`. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/ui/SettingsHome.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/settings/ui/SettingsHome.kt) | Add an App-group row with `FocusSwitch`; use localized strings. |
| [composeApp/src/commonMain/composeResources/values/strings.xml](../composeApp/src/commonMain/composeResources/values/strings.xml) and all `values-*` string files | Add label/subtitle for haptic feedback row. |
| [composeApp/src/androidMain/AndroidManifest.xml](../composeApp/src/androidMain/AndroidManifest.xml) | Add `android.permission.VIBRATE`. |
| [composeApp/src/androidMain/kotlin/com/focusritual/app/MainActivity.kt](../composeApp/src/androidMain/kotlin/com/focusritual/app/MainActivity.kt) | Initialize `AndroidHapticContext`. |

No Gradle dependency changes are expected: `multiplatform-settings` and material icon extended support are already present.

---

## 4. Detailed Implementation Steps

### Core Haptic

1. Add `HapticFeedbackType` with exactly `LightImpact`, `MediumImpact`, and `Success`.
2. Add a common `HapticEngine` interface plus `expect class PlatformHapticEngine() : HapticEngine`.
3. Add `HapticSettingsRepository`:
   - Back it with `Settings()`.
   - Default enabled state should be `true` unless product direction changes.
   - Key suggestion: `haptic_feedback_enabled`.
   - Expose `val hapticsEnabled: StateFlow<Boolean>`.
   - Provide `suspend fun setHapticsEnabled(enabled: Boolean)`.
   - Provide `companion object { val Default = HapticSettingsRepository() }` for shared default wiring.
4. Add `HapticController`:
   - Public methods: `soundTileEnabled()`, `mixLoaded()`, `sessionStarted()`.
   - Each method should return immediately when `hapticsEnabled.value` is false.
   - Map methods to feedback types:
     - `soundTileEnabled()` -> `LightImpact`.
     - `mixLoaded()` -> `MediumImpact`.
     - `sessionStarted()` -> `Success`.
   - Do not expose the engine to features.

### Settings Persistence, MVI, and UI

1. Extend `feature/settings/domain/SettingsRepository` to accept:
   - `audioSettingsRepository: AudioSettingsRepository = AudioSettingsRepository.Default`
   - `hapticSettingsRepository: HapticSettingsRepository = HapticSettingsRepository.Default`
2. Expose `hapticsEnabled` and `setHapticsEnabled(enabled)` from the settings feature repository.
3. Add `hapticsEnabled: Boolean = true` to `SettingsUiState`.
4. Add `data class SetHapticsEnabled(val enabled: Boolean) : SettingsIntent`.
5. Add `repository.hapticsEnabled` to the `combine(...)` in `SettingsViewModel` and handle the new intent in `viewModelScope.launch`.
6. Update `SettingsHome`:
   - Pass `uiState` into `AppGroup` or pass `hapticsEnabled` separately.
   - Add the haptics toggle inside `AppGroup`, ideally after language and before rate/share.
   - Use `FocusSwitch` and the existing row style.
   - Use localized resources, not hard-coded copy.
   - Do not fire a haptic when this settings toggle changes; approved haptic moments are intentionally limited.
7. Add localized strings to every `composeResources/values*/strings.xml` file:
   - `settings_app_haptics_label`: `Haptic feedback`
   - `settings_app_haptics_subtitle`: `Subtle taps for mix and session actions`
   - Localize equivalent copy for existing non-English files or use a conservative English fallback if the project currently accepts that pattern.

### App Wiring

1. In `App()`, create shared haptic dependencies with `remember`:
   - `val hapticSettingsRepository = remember { HapticSettingsRepository.Default }`
   - `val hapticController = remember { HapticController(settingsRepository = hapticSettingsRepository) }`
2. Pass `hapticController` into `MixerViewModel` through the existing `viewModel { ... }` factory path.
3. Pass `hapticController` into `FocusSessionScreen`.
4. Ensure `SettingsModal`/`SettingsViewModel` uses the same `hapticSettingsRepository` instance, either by passing a `SettingsViewModel` from `App()` or by relying on the same core `Default` singleton.
5. Keep all platform-specific construction inside the actual engine; `App.kt` should not branch by platform.

### Event Touch Points

1. **Sound tile toggled ON only** in `MixerViewModel`:
   - In `MixerIntent.ToggleSound`, read the current sound before toggling.
   - Fire only when the current sound exists and `isEnabled == false`.
   - Do not fire when toggling OFF.
   - Keep existing dirty-state behavior.
   - Suggested order: mark dirty -> toggle sound -> if previous disabled, call `hapticController.soundTileEnabled()`.
2. **Saved/preset mix loaded** in `MixerViewModel.loadMix(...)`:
   - If `presetId` is invalid, return with no haptic.
   - After `repo.loadSnapshot(...)` and loaded/dirty state updates, call `hapticController.mixLoaded()`.
   - Do not fire for save or delete.
3. **START SESSION tapped** in `FocusSessionScreen`:
   - In the `FocusSessionIntent.StartSession` branch, resolve the session config.
   - Call `hapticController.sessionStarted()` immediately before `onStartSession(config)`.
   - Keep this out of reusable `StartSessionButton` and out of the mixer hero/session entry button.

### Android Actuals

1. Add `AndroidHapticContext` using the same shape as `AndroidAudioContext`:
   - Store `applicationContext`.
   - Initialize from `MainActivity.onCreate` before Compose content.
2. Add `<uses-permission android:name="android.permission.VIBRATE" />` to `AndroidManifest.xml`.
3. Implement `PlatformHapticEngine.android.kt`:
   - Resolve `VibratorManager` on API 31+ and legacy `Vibrator` below that.
   - Return silently if no vibrator is available.
   - Use SDK checks only inside `androidMain`.
   - Map feedback:
     - `LightImpact`: short click/tick style effect.
     - `MediumImpact`: heavier click/impact style effect.
     - `Success`: short two-pulse waveform to approximate success notification.
4. Avoid leaking Activity context; use `AndroidHapticContext.appContext`.
5. The `VIBRATE` permission is normal permission; no runtime permission prompt should be needed.

### iOS Actuals

1. Implement `PlatformHapticEngine.ios.kt` with UIKit feedback generators:
   - `LightImpact`: `UIImpactFeedbackGenerator(style = UIImpactFeedbackStyleLight).impactOccurred()`.
   - `MediumImpact`: `UIImpactFeedbackGenerator(style = UIImpactFeedbackStyleMedium).impactOccurred()`.
   - `Success`: `UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeSuccess)`.
2. Call `prepare()` before triggering if available and local style allows it.
3. No Swift bridge is required for these UIKit APIs.
4. These APIs are older than iOS 18, so they do not change the existing iOS 18+ deployment constraint.

### Tests and Validation

1. Add `HapticControllerTest` with a fake `HapticEngine`:
   - Enabled setting maps `soundTileEnabled()` to one `LightImpact`.
   - Enabled setting maps `mixLoaded()` to one `MediumImpact`.
   - Enabled setting maps `sessionStarted()` to one `Success`.
   - Disabled setting produces no engine events.
2. Add repository tests if isolated settings support is available:
   - Default is enabled.
   - Setting false/true persists and updates the flow.
3. Add/extend mixer tests if a ViewModel test harness exists later:
   - Toggle OFF produces no haptic.
   - Toggle ON produces light haptic exactly once.
   - Loading a valid mix produces medium haptic.
   - Loading an invalid mix produces no haptic.
4. Manual validation on real devices is required for physical haptic feel, especially iOS success and Android waveform behavior.

---

## 5. Acceptance Criteria

- `core/haptic` contains the settings repository, controller, and expect/actual platform engine.
- `feature/settings` wraps the core haptic repository; `core/haptic` does not depend on `feature/settings`.
- Settings includes a persisted **Haptic feedback** toggle in the App group.
- No platform checks exist in `commonMain` haptic call sites.
- No production code outside `HapticController` calls `HapticEngine` or `PlatformHapticEngine`.
- With haptics enabled:
  - Enabling a disabled sound tile fires one light impact.
  - Disabling an enabled sound tile fires nothing.
  - Loading a valid saved mix fires one medium impact.
  - Starting a session from the session setup screen fires one success notification pattern.
- With haptics disabled, none of the three approved moments produce haptics.
- Android manifest includes `android.permission.VIBRATE`.
- Android app initializes the haptic context before any haptic can be triggered.
- iOS builds without Swift bridge changes.
- Localization resources are updated for the new settings row.

---

## 6. Validation Commands

Run from the repository root:

```bash
./gradlew :composeApp:allTests
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Recommended manual checks:
- Android physical device: verify light/medium/success feel and confirm disabled setting suppresses all haptics.
- iOS physical device: verify light/medium/success feel; simulator can validate build/navigation but not physical haptics.
- Settings relaunch: toggle off, kill/reopen app, verify the toggle remains off and haptics remain suppressed.

---

## 7. Risks and Notes

- Android haptic hardware and OEM behavior vary; the waveform may need tuning after testing on a real device.
- Some Android emulators and tablets may report no vibrator or produce no noticeable feedback.
- iOS simulator will not prove physical haptic feel.
- Keep the session-start haptic in the Start Session intent transition path to avoid duplicate triggers from recomposition or reusable button usage.
- Do not add haptics to the settings toggle itself; the approved scope intentionally limits feedback to the three ritual moments.
- If future haptic moments are added, route them through `HapticController` with explicit methods so the allowed surface remains auditable.