# FocusRitual — Roadmap & Architecture Decisions

## Completed
- Project scaffold: Kotlin Multiplatform + Compose Multiplatform.
- Custom dark FocusRitual theme and design system tokens.
- Mixer core: 9 bundled ambient sounds, independent toggles/volumes, organic motion, play/pause.
- Audio engine: expect/actual `AudioPlayer`, `SoundMixer`, `MixAudioOrchestrator`, session-aware master volume.
- Focus and sleep session setup screens with persisted custom preferences.
- Active session timer with phase management, breathing visuals, cycle progress, and audio fade behavior.
- Current mix modal and save mix dialog.
- Mixer persistence via `multiplatform-settings` + `kotlinx-serialization-json`: saved mixes and ambient snapshot.
- Full-screen Settings route with audio preferences, app/support/legal sections, sound credits, privacy policy, terms of use.
- Platform actions layer for app settings, rate/review, share, contact email, URL opening, app version; Settings emits one-shot effects to execute them.
- Android audio focus and iOS `AVAudioSession` integration for mix/duck settings.
- iOS Live Activity bridge and Widget extension structure.
- Protect Focus / Screen Time bridge skeleton.
- State-based root navigation in `App.kt` with hoisted `MixerViewModel`.

## Near-Term Priorities
1. Verify full-screen Settings/localization on devices: app-language settings on Android 13+, Preferred Language row on iOS, Settings detail/home back behavior, and iOS edge-swipe UX.
2. Review first-pass translations and replace generated legal/privacy/terms copy with final approved text.
3. Device-test audio mix/duck behavior with external audio on Android and iOS.
4. Device verification: test mix/duck behavior on real iOS and Android devices.
5. Production polish: final legal copy, real App Store ID, Protect Focus entitlements/device flow.
6. Add focused tests for SettingsViewModel, repositories, and ViewModel construction smoke coverage.
7. DI/navigation library only if default-arg construction or state-based routing becomes painful.

## Architecture Decisions

### State-based navigation for now
- `AppScreen` sealed interface + `Crossfade` keeps the app simple and allows `MixerViewModel` hoisting.
- Revisit if app-level back stack, deep links, settings/legal flows, or swipe-back behavior become complex.

### Hoisted mixer ViewModel
- `MixerViewModel` stays in `App.kt` so audio continues across mixer/session/timer screens.
- `setSessionMasterVolume(Float?)` lets session screens influence playback without owning audio.

### Multiplatform settings for local persistence
- The app currently uses `multiplatform-settings` for session preferences, audio settings, saved mixes, and ambient snapshot.
- JSON persistence lives in mixer `data/` and is intentionally local/offline.

### Custom audio player
- No mature KMP audio library covers FocusRitual's multi-track independent-volume requirements.
- Platform actuals remain straightforward and controlled.

### Platform actions as core abstraction
- External actions (settings, email, sharing, rate/review, URL opening) live behind `core/platformaction`.
- Feature ViewModels should emit effects; composables execute effects through `PlatformActions`.

### Koin remains deferred
- Default-argument construction is acceptable while dependency graphs are small.
- Add Koin when ViewModels/repositories become hard to instantiate or test.

### Ktor remains unnecessary
- The app is offline-first for v1. Add networking only for remote content, subscriptions, or analytics.
