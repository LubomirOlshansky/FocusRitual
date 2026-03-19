# FocusRitual — Roadmap & Architecture Decisions

## Completed (March 2026)
- [x] Project scaffolding (KMP + Compose Multiplatform)
- [x] Custom dark theme (FocusRitualTheme with 16 color tokens, typography scale)
- [x] MVI architecture for mixer feature (Contract, ViewModel, Screen)
- [x] Reusable PlayButton component (glassmorphic, animated)
- [x] Immersive gradient background (placeholder, no image assets)
- [x] Builds successfully on Android and iOS

## Next Steps (in priority order)
1. **Sound mixer sliders** — Add sound tiles with toggle + volume slider UI (no real audio)
2. **Audio playback** — expect/actual AudioPlayer abstraction (Android: ExoPlayer/MediaPlayer, iOS: AVAudioEngine)
3. **Navigation** — Add navigation-compose + kotlin-serialization for multi-screen support
4. **Timer screen** — Pomodoro timer with configurable durations
5. **DI** — Add Koin when ViewModels need injected dependencies (AudioPlayer, repositories)
6. **Persistence** — DataStore Preferences for settings, volumes, timer config
7. **Presets** — Save/load sound mixer configurations
8. **Settings screen** — Timer durations, notification preferences, etc.

## Architecture Decisions Log

### Decision: No Ktor (yet)
- App is fully offline for v1 (bundled sounds, local presets)
- Add Ktor only when remote content, subscriptions, or analytics are needed

### Decision: No Decompose/Voyager
- JetBrains navigation-compose is sufficient and first-party
- Less abstraction, same lifecycle ecosystem as existing deps

### Decision: Custom AudioPlayer (not a library)
- No mature KMP audio library supports multi-track mixing with independent volumes
- expect/actual with platform APIs (ExoPlayer + AVAudioEngine) is more flexible

### Decision: Koin for DI (when needed)
- Lightweight, KMP-native, no codegen
- Works with lifecycle ViewModel out of the box
- Hilt is Android-only, can't use in commonMain

### Decision: DataStore over multiplatform-settings
- First-party, same ecosystem as lifecycle deps
- Full KMP support since DataStore 1.1.0

### Decision: Defer AGP module split
- AGP 9.0 will require separating com.android.application from KMP module
- No action needed until AGP 9.0 ships — current setup works fine
