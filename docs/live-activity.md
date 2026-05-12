# Live Activity — iOS Setup Guide

## Overview

FocusRitual's Live Activity shows 3 distinct states on the lock screen and Dynamic Island:

| State | When shown | Visual feel |
|-------|-----------|-------------|
| **Ambient Playback** | Mix playing, no session | Calm, atmospheric, minimal |
| **Focus Session** | Focus timer active | Structured, intentional, progress ring |
| **Sleep Session** | Sleep timer active | Soft, gentle, drifting |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  KMP (iosMain)                                          │
│  ┌─────────────────┐  ┌──────────────────────────────┐  │
│  │ LiveActivityState│  │ LiveActivityController       │  │
│  │ · AmbientPlayback│──│ push(state) / stop()         │  │
│  │ · FocusActive    │  │ maps typed state → bridge    │  │
│  │ · SleepActive    │  └──────────┬───────────────────┘  │
│  └─────────────────┘              │                      │
│                        ┌──────────▼───────────────────┐  │
│                        │ LiveActivityBridge            │  │
│                        │ handler: LiveActivityHandler? │  │
│                        └──────────┬───────────────────┘  │
└───────────────────────────────────┼──────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────┐
│  Swift (Main App)                 │                      │
│                        ┌──────────▼───────────────────┐  │
│                        │ LiveActivityManager           │  │
│                        │ implements LiveActivityHandler│  │
│                        │ uses ActivityKit              │  │
│                        └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────┐
│  Widget Extension                 │                      │
│                        ┌──────────▼───────────────────┐  │
│                        │ FocusRitualLiveActivity       │  │
│                        │ dispatches by sessionType to: │  │
│                        │ · AmbientPlaybackView         │  │
│                        │ · FocusSessionActiveView      │  │
│                        │ · SleepSessionActiveView      │  │
│                        └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

## File Map

### KMP (iosMain)
| File | Purpose |
|------|---------|
| `core/liveactivity/LiveActivityState.kt` | Sealed interface — 3 typed states |
| `core/liveactivity/LiveActivityBridge.kt` | Handler protocol + singleton |
| `core/liveactivity/LiveActivityController.kt` | High-level push/stop API |

### Swift — Widget Extension (`iosApp/FocusRitualWidget/`)
| File | Purpose |
|------|---------|
| `FocusRitualAttributes.swift` | ActivityAttributes + ContentState |
| `FocusRitualLiveActivity.swift` | Widget — lock screen + Dynamic Island |
| `FocusRitualWidgetBundle.swift` | @main entry point |
| `RitualTokens.swift` | Design tokens (colors, typography) |
| `Views/AmbientPlaybackView.swift` | State 1 — ambient playback UI |
| `Views/FocusSessionActiveView.swift` | State 2 — focus session UI |
| `Views/SleepSessionActiveView.swift` | State 3 — sleep session UI |
| `Views/LiveActivityButton.swift` | Shared action button component |
| `LiveActivityPreviews.swift` | SwiftUI previews for all states |

### Swift — Main App (`iosApp/iosApp/`)
| File | Purpose |
|------|---------|
| `LiveActivityManager.swift` | ActivityKit lifecycle manager |
| `iOSApp.swift` | Wires manager into bridge at startup |

## Xcode Setup Required

### 1. Add Widget Extension Target

1. Open `iosApp.xcodeproj` in Xcode
2. File → New → Target → **Widget Extension**
3. Product Name: `FocusRitualWidget`
4. **Uncheck** "Include Configuration App Intent"
5. **Uncheck** "Include Live Activity" (we already have the code)
6. Finish

### 2. Configure the Extension

1. Delete the auto-generated template files from the new target
2. Add all files from `iosApp/FocusRitualWidget/` to the widget target
3. Add `FocusRitualAttributes.swift` to **both** the main app target and widget target (shared model)
4. Set the widget extension's deployment target to **iOS 18+**

### 3. Info.plist

Add to the **main app** `Info.plist`:
```xml
<key>NSSupportsLiveActivities</key>
<true/>
```

### 4. App Group

The main app and widget extension use an App Group plus Darwin notification to deliver Live Activity button actions to Kotlin while the app process is alive.

1. Add an App Group capability to both the main app and widget extension
2. Use the same group ID: `group.com.focusritual.app`

If you later want push-token based updates, reuse this shared capability for any additional shared state.

## Lifecycle Resilience

ActivityKit Live Activities are system-managed. FocusRitual treats stop/end actions as locally authoritative, but iOS does not guarantee that the app receives cleanup callbacks during a user force-quit or process kill.

Current behavior:
- Ambient Live Activity starts only when no session exists, the mixer is playing, and at least one sound is active.
- Pausing or stopping ambient playback falls through to the inactive path and asks ActivityKit to end existing FocusRitual activities.
- `LiveActivityManager` recovers existing `Activity<FocusRitualAttributes>.activities` instead of relying only on an in-memory activity reference.
- Starting a new FocusRitual Live Activity first ends existing FocusRitual activities so only one remains.
- Stop Mix and End Session widget intents end existing FocusRitual activities directly from the widget extension before notifying the main app.
- The app delegate performs bounded best-effort cleanup from `applicationWillTerminate`.

`staleDate` is only a system freshness hint. It should not be treated as an automatic dismissal guarantee, and force-quit cleanup is best-effort only.

## Usage from Kotlin

```kotlin
// In your ViewModel or coordinator:
val liveActivity = LiveActivityController()

// Start ambient
liveActivity.push(
    LiveActivityState.AmbientPlayback(
        mixSummary = "Rain • Wind",
        activeSoundCount = 3,
        isPaused = false,
    )
)

// Transition to focus
liveActivity.push(
    LiveActivityState.FocusActive(
        remainingSeconds = 1500,
        totalSeconds = 1500,
        phase = "Focus",
        currentCycle = 1,
        totalCycles = 4,
        mixSummary = "Rain • Wind",
        isPaused = false,
    )
)

// End
liveActivity.stop()
```

## Design Language

All 3 states share the dark cinematic palette from `Color.kt`:
- Background: `Surface` (#0C0E11) — near black
- Text: `OnSurface` (#E0E6F1) — soft white
- Secondary: `OnSurfaceVariant` (#A5ABB6) — muted grey
- Accent glow: `Primary` (#B7C8DB) — cool blue-grey, used only for subtle glows
- No bright colors, no hard borders, no system-player aesthetic

Each state has a distinct visual mood:
- **Ambient**: minimal, calm radial glow from left edge
- **Focus**: structured, progress ring halo, stronger glow
- **Sleep**: soft, centered diffuse glow, gentler hierarchy
