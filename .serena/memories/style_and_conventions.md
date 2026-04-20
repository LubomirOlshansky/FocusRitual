# FocusRitual — Code Style & Conventions

> **See also:**
> - `design_system` memory — canonical Design Language v1.0 (colors, typography, components, animation, motion/interaction/border rules, hard NEVERS, file map). All visual decisions must conform to it.
> - `project_structure` memory — concrete file layout & architecture per-feature.

---

## Architectural Invariants (enforced — non-negotiable)

These rules are verified via grep guardrails. Any PR that violates them must be rejected or refactored.

### Layer direction
```
app/  →  feature/  →  core/
```
- **`core/` MUST NOT import `feature/`.** Grep guardrail:
  `grep -rn "import com.focusritual.app.feature" composeApp/src/*/kotlin/com/focusritual/app/core/` → must return **0**.
- **`feature/<A>` MUST NOT import `feature/<B>`.** Features are siblings; they don't know about each other.
- **`app/integration/`** is the ONLY place where code may import from multiple features. Use it for composable "effects" or adapters that bridge features (e.g. Live Activity driver reading both mixer + timer state).
- `app/` may import anything. `core/` may import only other `core/` and stdlib/framework.

### Canonical feature shape (D1 of plans/structure-refactor.md)
Every feature under `feature/<name>/` follows this exact layout:
```
feature/<name>/
  <Name>Contract.kt        ← UiState data class + Intent sealed interface
  <Name>ViewModel.kt       ← exposes StateFlow<UiState> + onIntent()
  <Name>Screen.kt          ← stateful <Name>Screen + stateless <Name>ScreenContent
  ui/                      ← ALL internal sub-composables live here
    modal/                 ← (optional) modal/sheet sub-composables
  domain/                  ← models, repos, mappers, orchestrators, contracts
    usecase/               ← single-responsibility use cases
  data/                    ← (optional) persistence/network — none today
```
- Feature-root may also hold a second screen-level composable (e.g. `CurrentMixModal.kt`, `SessionCompleteScreen.kt`) — treat them as screens, not as `ui/` fragments.
- Small features may omit `ui/` or `domain/` until they're needed (e.g. `feature/about/`).

### Where does new code go?
| Scope | Location |
|---|---|
| UI sub-composable used only by feature X | `feature/<x>/ui/` |
| Reusable UI across features (truly generic) | `core/designsystem/component/` |
| Domain model/repo/use-case for feature X | `feature/<x>/domain/` |
| Cross-cutting platform API (expect/actual) | `core/<module>/` |
| Glue that reads from >1 feature's UiState | `app/integration/<topic>/` |
| Platform-specific actual | same path as expect, with `.ios.kt` / `.android.kt` suffix |

### Platform actuals
- File name: `<Name>.<platform>.kt` (e.g. `Platform.ios.kt`, `AudioPlayer.android.kt`, `LiveActivityEffect.android.kt`).
- Package must match the `expect` declaration exactly.

---

## Kotlin Style
- Follow **Kotlin official code style** (`kotlin.code.style=official` in gradle.properties)
- Root package: `com.focusritual.app`
- Feature packages: `com.focusritual.app.feature.<feature_name>[.ui|.domain|.domain.usecase]`
- Core packages: `com.focusritual.app.core.<module_name>`
- Integration packages: `com.focusritual.app.app.integration.<topic>`

## Naming Conventions
- **Files:** PascalCase matching the primary class/composable (`MixerScreen.kt`, `PlayButton.kt`)
- **Contract files:** `<Feature>Contract.kt` — `UiState` data class + `Intent` sealed interface
- **ViewModel files:** `<Feature>ViewModel.kt`
- **Screen files:** `<Feature>Screen.kt`
- **Use-case files:** `<Verb><Noun>UseCase.kt` (e.g. `AdjustVolumeUseCase.kt`)
- **Theme files:** `Color.kt`, `Type.kt`, `Theme.kt`, `Spacing.kt`, `Motion.kt`
- **Platform actuals:** `<Name>.<platform>.kt`

## Compose Conventions
- Top-level composables are `@Composable` functions in PascalCase
- Stateful composable: takes ViewModel, collects state — `MixerScreen(viewModel)`
- Stateless composable: takes UiState + event lambdas — `MixerScreenContent(uiState, onIntent)`
- Internal sub-composables live in `feature/<x>/ui/` with `internal` visibility
- Feature code may **only** use `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `Spacing.*`, `FocusRitualEasing.*` — never reference raw `Color.kt` tokens directly
- Hardcoded hex (`Color(0xFF…)`) is forbidden in feature code (single documented exception: `Color(0xFF2A3240)` in `SessionModeToggle`)
- No `Color.White`, no `Color.Black` anywhere
- No Material ripple — set `indication = null` on every clickable; use `scale(0.97f)` press feedback
- LazyColumn items: hoist per-item lambdas via `remember(id, onIntent) { … }` to avoid per-frame allocations

## MVI Pattern
- **UiState:** `@Immutable data class` with sensible defaults; single source of truth for the screen
- **Intent:** Sealed interface; each action is a `data object` or `data class`
- **ViewModel:** extends `ViewModel()`; exposes `val uiState: StateFlow<UiState>` + single `fun onIntent(intent: Intent)` entry point
- **State updates:** `_uiState.update { it.copy(...) }` (never mutate in place)
- **Side effects:** launched inside `viewModelScope`, NEVER inside `MutableStateFlow.update {}` lambdas (learned in mixer refactor — update blocks must be pure)
- **Property init order:** don't reference `lateinit` properties from class-level property initializers (e.g. `combine(…)`). Use a `MutableStateFlow` bridge if a dependency is only available after `start()`/`init{}`.

## Domain Layer Conventions
- **Repositories:** expose `StateFlow<T>` + `update { }` mutator; keep persistence/audio side-effects OUT.
- **Use cases:** one class per action; take repo or flow in constructor; expose a single `operator fun invoke(args)`.
- **Mappers:** pure top-level `fun` in a `<Feature>Mappers.kt` file — no state, no side effects, deterministic.
- **DTOs:** `@Immutable data class` in `<Feature>Dtos.kt` — view-oriented projections of domain state.

## Testing
- Tests live in `commonTest/kotlin/com/focusritual/app/**` (flat structure — NOT mirrored to production packages per plan D7).
- Cover: mappers (pure), use cases (with fake repos), audio lifecycle (with `FakeAudioPlayerFactory`), ViewModel construction (to catch property-init ordering bugs).
- `AudioPlayer` is `expect class` → NOT subclassable in commonTest. Use `AudioPlayerHandle` interface + `FakeAudioPlayerFactory` instead.

## General Rules
- Keep everything in `commonMain` unless a platform-specific API is needed
- Use `expect`/`actual` for platform abstractions
- No Android-specific imports in commonMain
- Minimal, clean code — avoid over-engineering
- No unnecessary comments, docstrings, or type annotations on obvious types
- `@OptIn(ExperimentalForeignApi::class)` is required for UIKit interop in iosMain
- Avoid mixing `Modifier.padding(horizontal=, bottom=)` in a single call (Compose pitfall — see `/memories/repo/build-validation.md`)
