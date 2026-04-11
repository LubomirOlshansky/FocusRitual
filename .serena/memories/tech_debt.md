# FocusRitual — Tech Debt

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
- [ ] **Minimum iOS version**: FamilyControls requires iOS 16.0. Verify the project's minimum deployment target matches.
- [ ] **ProtectFocusSetupSheet drag handle alpha**: Was changed from 0.15f → 0.12f during visual refinement but the memory in design_system doesn't document this.

## Audio

- [ ] **Temp file cleanup (Android)**: AudioPlayer.android.kt creates temp files for playback. No cleanup mechanism on app exit unless the OS handles it.

## General

- [ ] **No navigation library**: State-based routing in App.kt works for 3 screens but will become unwieldy with more. Consider Voyager or Decompose if screen count grows.
- [ ] **No dependency injection**: ViewModels are created inline. Consider a simple DI setup if controller/repository count grows.
