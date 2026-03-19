# FocusRitual — Project Structure & Architecture

## Directory Layout

```
FocusRitual/                         ← Root (Gradle root project)
├── build.gradle.kts                 ← Root build file (no plugins applied directly)
├── settings.gradle.kts              ← Includes :composeApp, sets up repositories
├── gradle.properties                ← Kotlin/Gradle/Android properties
├── gradle/
│   ├── libs.versions.toml           ← Version catalog (ALL deps defined here)
│   └── wrapper/gradle-wrapper.properties
│
├── composeApp/                      ← Main shared module (KMP + Compose)
│   ├── build.gradle.kts             ← KMP plugin config, source sets, Android app config
│   └── src/
│       ├── commonMain/kotlin/com/focusritual/app/
│       │   ├── App.kt              ← Root @Composable: FocusRitualTheme { MixerScreen() }
│       │   ├── Platform.kt         ← expect fun getPlatform(): Platform
│       │   ├── core/               ← Reusable design system (theme + components)
│       │   └── feature/            ← Feature modules (mixer, future: timer, settings)
│       │
│       ├── androidMain/kotlin/com/focusritual/app/
│       │   ├── MainActivity.kt     ← Android entry point, calls App()
│       │   └── Platform.android.kt ← actual fun getPlatform()
│       │
│       └── iosMain/kotlin/com/focusritual/app/
│           ├── MainViewController.kt ← ComposeUIViewController { App() }
│           └── Platform.ios.kt       ← actual fun getPlatform()
│
└── iosApp/                          ← iOS Xcode project (thin SwiftUI wrapper)
    ├── Configuration/Config.xcconfig
    ├── iosApp/
    │   ├── iOSApp.swift             ← @main SwiftUI App, renders ContentView
    │   ├── ContentView.swift        ← UIViewControllerRepresentable bridging to Compose
    │   └── Info.plist
    └── iosApp.xcodeproj/            ← Xcode project files
```

## Implemented Feature Structure (as of March 2026)

All shared code lives in `composeApp/src/commonMain/kotlin/com/focusritual/app/`:

```
com/focusritual/app/
├── App.kt                          ← Root composable: FocusRitualTheme { MixerScreen() }
├── Platform.kt                     ← expect/actual platform interface
│
├── core/
│   └── designsystem/
│       ├── theme/
│       │   ├── Color.kt            ← 16 color tokens from design system (dark palette)
│       │   ├── Type.kt             ← FocusRitualTypography (5 text styles, system font)
│       │   └── Theme.kt            ← FocusRitualTheme wrapping MaterialTheme with darkColorScheme
│       └── component/
│           └── PlayButton.kt       ← Reusable 96dp circular glassmorphic play/pause button
│
└── feature/
    └── mixer/
        ├── MixerContract.kt        ← MixerUiState + MixerIntent (MVI contract)
        ├── MixerViewModel.kt       ← ViewModel with StateFlow<MixerUiState>, onIntent()
        └── MixerScreen.kt          ← MixerScreen (stateful) + MixerScreenContent (stateless) + ImmersiveBackground

Deleted: Greeting.kt (template boilerplate, removed)
```

## Future Feature Folders (not yet created)
- `feature/timer/` — Pomodoro timer screen
- `feature/settings/` — App settings
- `feature/presets/` — Preset management
- `core/audio/` — expect/actual AudioPlayer abstraction
- `core/data/` — Persistence layer (DataStore/Room)

## Architecture: MVI Pattern

- **State:** `MixerUiState` data class — single source of truth, exposed via `StateFlow`
- **Intent:** `MixerIntent` sealed interface — all user actions
- **ViewModel:** Single `onIntent(intent: MixerIntent)` entry point, updates StateFlow
- **Screen split:** Stateful composable (owns ViewModel) + Stateless content (receives state + lambdas)
- **Theme:** Always access colors/typography via `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*`

## Source Set Rules

| Code Type | Source Set |
|-----------|-----------|
| UI (Composables) | commonMain |
| Business logic / ViewModels | commonMain |
| Data models | commonMain |
| Theme / design system | commonMain |
| Platform APIs (camera, notifications) | expect in commonMain, actual in androidMain/iosMain |
| Android-only (Activity, Context) | androidMain |
| iOS-only (UIKit interop) | iosMain |

## Entry Points

- **Android:** `MainActivity.onCreate()` → `setContent { App() }`
- **iOS:** `MainViewController()` → `ComposeUIViewController { App() }`
- **App.kt:** `FocusRitualTheme { MixerScreen() }`
