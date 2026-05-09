# FocusRitual — Tech Debt

## Architecture / Structure
Structure refactor is complete and layer invariant is enforced: `app/` → `feature/` → `core/`; cross-feature glue belongs in `app/integration/`.

### Remaining
- [ ] **Localization and legal translations need review** — shared Compose string resources now exist for `en`, `es`, `fr`, `de`, `pt-BR`, `zh-Hans`, `ja`, `ko`, `hi`, and `ar`, but translations are first-pass and need native-speaker review; legal/privacy/terms text still needs final legal approval.
- [ ] **iOS Preferred Language needs device verification** — `CFBundleLocalizations`, `.lproj/InfoPlist.strings`, and app-settings routing are wired, but the iOS Settings Preferred Language row still needs simulator/device confirmation after Xcode build.
- [ ] **Android app-language settings need device verification** — `android:localeConfig` and locale config XML are wired; confirm Android 13+ opens app-language settings and older Android falls back cleanly.
- [ ] **iOS swipe-back needs manual UX testing** — edge-strip Compose handler is implemented for iOS-first back navigation. Verify it does not conflict with Settings scrolling/taps and that it closes details/home in the intended order.
- [ ] **Settings audio behavior needs device verification** — mix/duck settings and `mixWithOthersVolume` are wired through shared audio settings, Android audio focus, and iOS `AVAudioSession`; still needs real-device testing with external audio.
- [ ] **iOS App Store ID placeholder remains in platform actions** — replace `0000000000` with the production App Store ID before release.
- [ ] **No dependency injection** — ViewModels still rely on inline/default-arg constructor chains. Koin remains deferred but should be revisited as repositories/controllers grow.
- [ ] **No navigation library/back stack** — `AppScreen` + `Crossfade` still works, and Settings is handled as a local draggable modal overlay; deeper future flows may justify a navigator/back-stack abstraction.
- [ ] **`feature/timer/AtmosphericField.kt` has raw hex literals** — migrate file-scope color constants into design tokens or `MaterialTheme.colorScheme` usage.
- [ ] **`FocusSessionScreen.kt` is large** — candidate for further `feature/session/ui/` extraction once touched again.
- [ ] **Possible module split** — `composeApp` is still one module. Not urgent, but feature boundaries now make extraction plausible.

## Protect Focus / Screen Time

### P0 — Blocking before production
- [ ] **FamilyControls entitlement** — request from Apple Developer portal; without it, production authorization will fail/crash.
- [ ] **ScreenTimeManager.swift target membership** — confirm it is included in the iOS app target.
- [ ] **Family Controls capability** — enable in Xcode Signing & Capabilities.
- [ ] **Physical device testing** — FamilyControls is not simulator-friendly.

### P1 — Before feature is user-ready
- [ ] **Simulator debug stub** — fake ScreenTimeHandler for auth/picker flows without entitlement.
- [ ] **Persist FamilyActivitySelection** — current Swift selection is in-memory only.
- [ ] **Pass selection back to Kotlin** — bridge selected-app state or a configured boolean.
- [ ] **Configured/active ProtectFocusCard state** — show shield active and blocked-app count/status.
- [ ] **PermissionDenied UX** — provide inline guidance or route to Settings.

### P2 — Polish
- [ ] **Picker dismiss animation overlap** — native picker and Compose sheet can overlap briefly.
- [ ] **ProtectFocusSetupSheet drag handle alpha** — document or align with design memory.

## Audio
- [ ] **Android temp audio-file cleanup** — `AudioPlayer.android.kt` creates temp files for playback; add cleanup strategy if profiling/device storage shows accumulation.
- [ ] **ViewModel construction smoke tests** — add trivial construction tests for Mixer/Settings/session/timer ViewModels to catch property-init ordering regressions.

## Resolved
- ~~`core/` imports `feature/`~~ → fixed in structure refactor.
- ~~Mixer micro-folder sprawl~~ → flattened into canonical feature shape.
- ~~Timer large flat folder~~ → split into root + `ui/`.
- ~~Android LiveActivity actual missing `.android.kt` suffix~~ → renamed.
- ~~Organic engine side effects inside `MutableStateFlow.update {}`~~ → moved into orchestrator collect/launch bodies.
- ~~`SoundIcon` enum + `toImageVector()` OCP issue~~ → icons embedded in catalog data.
- ~~`AudioPlayer` expect-class untestable~~ → `AudioPlayerHandle` + fake factory introduced.
- ~~`SessionPreferences.kt` was a stub~~ → now uses `multiplatform-settings` delegated ints.
- ~~Settings screen missing~~ → replaced old About sheet with `feature/settings`.
- ~~Settings bottom sheet/full-screen route churn~~ → settled as a draggable `SettingsModal` overlay above Mixer with top-right close.
- ~~Settings platform actions bypassed MVI/effects~~ → Language/Rate/Share/Contact now emit `SettingsEffect` and execute via `PlatformActions` at the composable boundary.
- ~~Settings detail state could reopen stale~~ → `SettingsScreen` resets to home on open and detail back is explicit.
- ~~Empty `PlatformActionHandler*` files~~ → removed.
- ~~Settings row divider/design deviations~~ → dividers removed and touched Settings outline alpha aligned.
- ~~Saved mixes/presets not persisted~~ → mixer data repositories persist saved mixes and ambient snapshot.
- ~~App localization infrastructure missing~~ → shared Compose string resources and Android/iOS locale declarations added for the first ten locales.
