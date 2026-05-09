# FocusRitual — Code Style & Conventions

See also: `design_system` for visual rules and `project_structure` for current layout.

## Architectural Invariants

Layer direction:
```
app/  →  feature/  →  core/
```
- `core/` must not import `feature/`.
- Features must not import sibling features.
- Cross-feature effects/adapters belong under `app/integration/`.
- `app/` may compose features and own root navigation.

## Canonical Feature Shape
```
feature/<name>/
  <Name>Contract.kt        ← UiState + Intent (+ Effect when needed)
  <Name>ViewModel.kt       ← StateFlow<UiState> + onIntent()
  <Name>Screen.kt          ← stateful + stateless split, or screen-level modal/sheet
  ui/                      ← internal sub-composables
    modal/                 ← optional modal pieces
  domain/                  ← models, repos, mappers, orchestrators, contracts
    usecase/               ← single-action use cases
  data/                    ← persistence/network implementation when needed
```
Feature-root may hold screen-level composables such as `CurrentMixModal`, `SaveMixDialog`, or `SettingsSheet`.

## Placement Rules
| Scope | Location |
|---|---|
| Feature-local UI | `feature/<x>/ui/` |
| Reusable UI | `core/designsystem/component/` |
| Feature domain model/repo/use case | `feature/<x>/domain/` |
| Feature persistence implementation | `feature/<x>/data/` |
| Cross-cutting platform API | `core/<module>/` |
| Multi-feature glue | `app/integration/<topic>/` |
| App-level navigation helper | `app/navigation/` or `core/navigation/` depending on whether it imports feature contracts |

## Kotlin Style
- Kotlin official style (`kotlin.code.style=official`).
- Root package: `com.focusritual.app`.
- PascalCase files matching primary symbols.
- Platform actuals use `<Name>.<platform>.kt` and matching package declarations.
- Keep comments sparse and useful.

## Compose Style
- Stateful composable collects state and owns ViewModel lookup.
- Stateless content takes `UiState` and lambdas/intent dispatchers.
- Internal sub-composables in `ui/` should be `internal`.
- Use `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `Spacing.*`, `FocusRitualEasing.*` in feature UI.
- No raw `Color(0xFF...)` in feature code except documented design-system exceptions.
- No `Color.White` / `Color.Black`.
- No Material ripple; custom clickables use `indication = null` with press-scale feedback.
- Avoid mixed `Modifier.padding(horizontal = ..., bottom = ...)`; use explicit `start`/`end` when combined with one-sided padding.

## MVI Pattern
- `UiState`: `@Immutable data class` with defaults.
- `Intent`: sealed interface using `data object` / `data class`.
- `ViewModel`: exposes `val uiState: StateFlow<UiState>` and one `onIntent(intent)`.
- State updates use `MutableStateFlow.update { ... }`; update lambdas must be pure.
- Side effects run in coroutines outside `update {}`.
- For platform actions from a feature, prefer `SettingsEffect`/`FeatureEffect` emitted by the ViewModel and collected by the composable, which then calls `PlatformActions`. Do not inject UIKit/Android APIs into common ViewModels.
- Reset transient feature state explicitly on dismiss when a sheet/modal ViewModel can outlive the visible sheet.

## Domain/Data Conventions
- Repositories expose `StateFlow<T>` plus narrow mutators.
- Persistence implementation belongs in `data/`; domain/UI should not know JSON/key details.
- Mappers are pure top-level functions.
- DTOs and domain state are immutable data classes.

## Platform Abstractions
- Use `expect`/`actual` for platform APIs.
- UIKit/AVFoundation interop in `iosMain` needs `@OptIn(ExperimentalForeignApi::class)`.
- Platform action providers live in `core/platformaction`; feature code should depend on the `PlatformActions` interface only at the composable effect boundary.

## Testing
- Tests live in `commonTest/kotlin/com/focusritual/app/**`.
- Cover pure mappers/use cases, repository behavior, audio lifecycle, and ViewModel construction smoke tests.
- `AudioPlayer` is an `expect class`; use `AudioPlayerHandle` + fake factory for tests.
