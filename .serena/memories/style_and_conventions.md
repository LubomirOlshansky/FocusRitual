# FocusRitual — Code Style & Conventions

## Kotlin Style
- Follow **Kotlin official code style** (`kotlin.code.style=official` in gradle.properties)
- Package: `com.focusritual.app` with sub-packages for features and core modules
- Feature packages: `com.focusritual.app.feature.<feature_name>`
- Core packages: `com.focusritual.app.core.<module_name>`

## Naming Conventions
- **Files:** PascalCase matching the primary class/composable (e.g., `MixerScreen.kt`, `PlayButton.kt`)
- **Contract files:** `<Feature>Contract.kt` containing UiState data class + Intent sealed interface
- **ViewModel files:** `<Feature>ViewModel.kt`
- **Screen files:** `<Feature>Screen.kt`
- **Theme files:** `Color.kt`, `Type.kt`, `Theme.kt`
- **Platform actuals:** `<Name>.<platform>.kt` (e.g., `Platform.android.kt`, `Platform.ios.kt`)

## Compose Conventions
- Top-level composables are `@Composable` functions in PascalCase
- Stateful composable: takes ViewModel, collects state — e.g., `MixerScreen(viewModel)`
- Stateless composable: takes UiState + event lambdas — e.g., `MixerScreenContent(uiState, onIntent)`
- Private composables for screen-internal sections (e.g., `ImmersiveBackground()`)
- Always use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` — never reference Color.kt tokens directly in feature code

## MVI Pattern
- **UiState:** Immutable data class with sensible defaults
- **Intent:** Sealed interface, each action is a `data object` or `data class`
- **ViewModel:** Extends `ViewModel()`, exposes `val uiState: StateFlow<UiState>`, single `fun onIntent(intent: Intent)` entry
- **State updates:** Use `_uiState.update { it.copy(...) }` pattern

## General Rules
- Keep everything in `commonMain` unless platform-specific API is needed
- Use `expect`/`actual` for platform abstractions
- No Android-specific imports in commonMain
- Minimal, clean code — avoid over-engineering
- No unnecessary comments, docstrings, or type annotations on obvious types
- No `#ffffff` — use off-white tones per design system
