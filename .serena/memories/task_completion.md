# FocusRitual — Task Completion Checklist

When a coding task is completed, verify the following:

## 1. Compilation & Tests
- `./gradlew :composeApp:compileDebugKotlinAndroid` — Android compiles
- `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` — iOS framework links
- `./gradlew :composeApp:allTests` — all tests pass (both Android unit + iosSimulatorArm64)
- Fix any compiler errors or test failures before declaring task complete

## 2. Architectural Invariants (MUST be green)
- `grep -rn "import com.focusritual.app.feature" composeApp/src/*/kotlin/com/focusritual/app/core/` → **0 matches**
  - `core/` must not import `feature/`. If violated, move the file to `feature/<x>/` (feature-specific) or `app/integration/` (cross-feature).
- No `feature/<A>` imports `feature/<B>`. Cross-feature code goes in `app/integration/<topic>/`.
- New files placed in the canonical feature shape:
  - Feature root only for `<Name>Contract.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt` (and optionally a second screen-level composable)
  - Sub-composables → `feature/<x>/ui/` (or `ui/modal/`) with `internal` visibility
  - Models / repos / mappers / orchestrators / use cases → `feature/<x>/domain/` (+ `domain/usecase/`)

## 3. Code Quality
- No Android-specific imports in `commonMain`
- Composables use `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` / `Spacing.*` / `FocusRitualEasing.*` — never raw `Color.kt` tokens or `Color(0xFF…)` literals (one documented exception in `SessionModeToggle`)
- No `Color.White`, no `Color.Black`, no Material ripple (use `indication = null` + `scale(0.97f)`)
- MVI pattern: `UiState` data class, `Intent` sealed interface, `ViewModel` exposing `StateFlow<UiState>` + single `onIntent()`
- Stateful/stateless composable split preserved: `<Name>Screen(viewModel)` + `<Name>ScreenContent(uiState, onIntent)`
- No side-effects inside `MutableStateFlow.update {}` lambdas — update blocks must be pure
- Don't reference `lateinit` properties from class-level property initializers (e.g. `combine(…)`); use a `MutableStateFlow` bridge

## 4. File & Package Hygiene
- File names match the primary symbol (PascalCase)
- Package declaration matches directory structure exactly
- Platform actuals use `<Name>.<platform>.kt` suffix (`.ios.kt`, `.android.kt`)
- No unused imports
- Kotlin official code style

## 5. LiveActivity / Bridge Changes
- Touched both Kotlin side (`iosMain core/liveactivity/` or `app/integration/liveactivity/`) AND Swift side (`iosApp/FocusRitualWidget/FocusRitualAttributes.swift`, `iosApp/LiveActivityManager.swift`) as needed
- Verified handler registration still happens at Swift app startup

## 6. Mixer / UiState Contract
- `MixerUiState.{isPlaying, activeSoundsSummary, activeSoundCount}` preserved — Live Activity depends on these exact fields
- `App.kt` remains the VM-hoisting site; VM API surface unchanged unless the task explicitly changes it

## 7. Tests
- Pure logic (mappers, use cases) has commonTest coverage
- For new ViewModels, add at minimum a construction smoke test (`MixerViewModelInitTest` pattern) to catch property-init ordering bugs
- Test files live flat under `commonTest/kotlin/com/focusritual/app/**` (NOT mirrored to production packages)

## 8. Memory Sync
- If the task added a new top-level folder, feature, or architectural rule, update `project_structure` and/or `style_and_conventions` Serena memories
- If the task resolved tech debt, move the item from `tech_debt` "Remaining" to the "Resolved" section
