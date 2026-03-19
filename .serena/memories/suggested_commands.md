# FocusRitual — Suggested Commands

## System: macOS (Darwin)

## Build Commands

### Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Build release APK
./gradlew :composeApp:assembleRelease

# Install debug APK to connected device/emulator
./gradlew :composeApp:installDebug
```

### iOS
```bash
# Compile iOS framework (simulator)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Open Xcode project (then build/run from Xcode)
open iosApp/iosApp.xcodeproj
```

### General Gradle
```bash
# Clean build
./gradlew clean

# Full build (all targets)
./gradlew build

# Check compilation without full build
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:compileDebugKotlin

# Run tests
./gradlew :composeApp:allTests
```

## Dependency Management
- All versions defined in: `gradle/libs.versions.toml`
- Referenced in build.gradle.kts as: `libs.plugins.*`, `libs.*`, `libs.versions.*`

## Project Navigation
```bash
# Source roots
ls composeApp/src/commonMain/kotlin/com/focusritual/app/
ls composeApp/src/androidMain/kotlin/com/focusritual/app/
ls composeApp/src/iosMain/kotlin/com/focusritual/app/

# iOS wrapper
ls iosApp/iosApp/

# Gradle config
cat gradle/libs.versions.toml
cat composeApp/build.gradle.kts
```

## Useful Utilities
```bash
# Search for Kotlin symbols
grep -r "class \|fun \|object " composeApp/src/commonMain/kotlin/ --include="*.kt"

# Find files by name
find composeApp/src -name "*.kt" | head -20

# Check git status
git status
git diff --stat
```
