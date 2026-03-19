# FocusRitual — Task Completion Checklist

When a coding task is completed, verify the following:

## 1. Compilation Check
- Run `./gradlew :composeApp:compileDebugKotlin` to verify Android compilation
- Run `./gradlew :composeApp:compileKotlinIosSimulatorArm64` to verify iOS compilation
- Fix any compiler errors before declaring task complete

## 2. Code Quality
- Ensure no Android-specific imports in `commonMain` source set
- Ensure all composables use `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` (not raw color tokens)
- Ensure MVI pattern is followed: UiState, Intent, ViewModel with StateFlow
- Ensure stateful/stateless composable split is maintained

## 3. Structure
- New feature code goes in `feature/<feature_name>/` package
- Reusable UI goes in `core/designsystem/component/`
- Theme tokens go in `core/designsystem/theme/`
- No file is placed in the root `com.focusritual.app` package unless it's the top-level `App.kt` or `Platform.kt`

## 4. Consistency
- File names match their primary symbol (PascalCase)
- Package declarations match directory structure
- No unused imports
- Follow Kotlin official code style
