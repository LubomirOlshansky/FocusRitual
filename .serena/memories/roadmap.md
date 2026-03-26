# FocusRitual — Roadmap & Architecture Decisions

## Completed (March 2026)
- [x] Project scaffolding (KMP + Compose Multiplatform)
- [x] Custom dark theme (FocusRitualTheme with 17 color tokens, typography scale)
- [x] MVI architecture for mixer feature (Contract, ViewModel, Screen)
- [x] Reusable PlayButton component (glassmorphic, animated)
- [x] Immersive background with dark forest image + gradient overlay
- [x] Sound mixer: 9 ambient sounds with toggle + volume, SoundMixer orchestrator
- [x] Audio playback: expect/actual AudioPlayer (Android: MediaPlayer, iOS: AVAudioPlayer)
- [x] Focus session configuration screen (3 presets + custom with steppers)
- [x] Active focus session timer (countdown, phase management, breathing circle, progress dots)
- [x] State-based navigation (AppScreen sealed interface + Crossfade)
- [x] Phase-aware sound control (Focus=play, Break=silent, fade transitions, cleanup on exit)
- [x] SessionConfig flow: FocusSessionScreen → resolveConfig() → ActiveSessionScreen
- [x] MixerViewModel hoisting for audio continuity across screens
- [x] Builds successfully on Android and iOS

## Next Steps (in priority order)
1. **DI** — Add Koin when ViewModels need injected dependencies
2. **Persistence** — DataStore Preferences for saved sound mixes, timer configs
3. **Presets** — Save/load sound mixer configurations
4. **Settings screen** — Timer defaults, notification preferences
5. **Notifications** — Timer phase change alerts (platform-specific)
6. **Polish** — Haptic feedback, additional sounds, onboarding

## Architecture Decisions Log

### Decision: State-based navigation (not navigation-compose)
- Simple AppScreen sealed interface + Crossfade in App.kt
- Allows hoisting MixerViewModel for audio continuity across screens
- No serialization needed, no library dependency
- Works well for 3-screen app; reconsider if screen count grows significantly

### Decision: Hoisted MixerViewModel for audio continuity
- MixerViewModel created in App.kt, passed to MixerScreen and indirectly controls audio during sessions
- `setSessionMasterVolume(Float?)` allows ActiveSessionScreen to control playback without owning SoundMixer
- `null` returns control to mixer's own isPlaying state

### Decision: No Ktor (yet)
- App is fully offline for v1 (bundled sounds, local presets)
- Add Ktor only when remote content, subscriptions, or analytics are needed

### Decision: Custom AudioPlayer (not a library)
- No mature KMP audio library supports multi-track mixing with independent volumes
- expect/actual with platform APIs (MediaPlayer + AVAudioPlayer) is more flexible
- SoundMixer.syncState() declaratively syncs UI state to audio playback

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
