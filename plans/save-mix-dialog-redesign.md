# SaveMixDialog Single-File Redesign Plan

## Goal

Rewrite only [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt) so the dialog keeps the existing caller contract, saves immediately, and closes with no confirmation crossfade, no saved screen, and no Done state.

## Constraints

- Edit only [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt).
- Do not require changes to [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt), [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt), [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt), or [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt).
- Keep the current public API source-compatible even if some inputs become compatibility-only.
- Use only `MaterialTheme.colorScheme.*`, `Spacing.*`, and existing motion tokens. No raw hex, no `Color.White`, no `Color.Black`.
- Keep custom no-ripple interactions and 0.5dp borders.

## Existing Contract

- `SaveMixDialog` currently takes `activeSounds`, `dialogState`, `existingMixNames`, `onSave`, `onDone`, `onDismiss`, and `modifier`.
- [CurrentMixModal.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) owns the dialog visibility and `SaveDialogState`. Its `onSave` lambda dispatches `MixerIntent.SaveCurrentMix(name)` and flips the state to `Saved`. Its `onDone` lambda closes the dialog, resets the state to `Input`, and calls `onSaveComplete()`.
- [MixerScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) passes `onSaveComplete = { showCurrentMixModal = false }`, so the current `onDone` path closes the parent modal too.
- `existingMixNames` is currently derived from saved mixes as trimmed lowercase names in [MixerScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt), but the dialog should validate case-insensitively on its own instead of depending on that normalization detail.
- [MixerViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) saves synchronously in memory, so an immediate close after `onSave` is compatible with the current implementation.

Single-file viability: yes. Keep the signature and enum intact for callers, render only the input form, and submit by calling `onSave(trimmed)` immediately followed by `onDone()`.

## Required Changes

- Remove the internal two-state flow from [SaveMixDialog.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt): no content `Crossfade`, no `SavedContent`, no `SavedRings`, no `Ring`, no `SavedDoneButton`, no delay-based confirmation logic.
- Keep `SaveDialogState` and the `dialogState` parameter only for compatibility with [CurrentMixModal.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt). The dialog body should always render the input state.
- Keep `BasicTextField` with 40-character max length, single-line input, word capitalization, IME Done handling, and automatic focus request when the dialog appears.
- Keep the active-sound preview pills, but restyle them with existing color-scheme tokens instead of the current file-local hex constants.
- Keep duplicate-name validation inline. Compute it inside the dialog with a case-insensitive trimmed comparison so the file remains robust if caller normalization changes later.
- Keep save disabled for blank or duplicate names. The save button and IME Done should share the same submit path.
- Change submit behavior so a valid save immediately triggers `onSave(trimmed)` and then `onDone()`. Cancel and scrim dismiss should continue to call `onDismiss()`.
- Replace current hardcoded surfaces with token-based mappings:
  - Dialog card: closest dark floating token, most likely `surfaceContainerHigh` with opacity tuned as needed.
  - Input background: `surface`.
  - Sound pill background: `surface` or `surfaceContainerLow`, whichever best preserves the recessed look without introducing a new color.
  - Borders: `outlineVariant` at or below design-system alpha limits.
  - Focus border and enabled save state: `primary` alpha variations only.
- Remove any bright filled CTA treatment that depends on `onPrimary` if it conflicts with FocusRitual's subdued button language. Prefer the existing low-alpha primary treatment already used elsewhere in the app.
- Keep motion minimal inside the file. After removing the confirmation state, the only remaining animation should be focused-border transitions, button color transitions, and optional 150ms press-scale feedback.

Local implementation notes:

- If `FocusRequester.requestFocus()` proves flaky while the dialog animates in on iOS, fix it inside this file with a tiny deferred request rather than widening scope.
- Because callers still write `SaveDialogState.Saved`, the redesign must ignore that state safely rather than deleting it.
- Immediate `onSave` then `onDone` is safe only because the current save path is synchronous. If save ever becomes async, this contract will need to change in a later task.

## Implementation Steps

1. Simplify [SaveMixDialog.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixDialog.kt) so `DialogCard` always renders one input-content branch and no longer switches on `dialogState`.
2. Delete the saved-confirmation helpers and their imports, constants, and animation code.
3. Centralize submit handling in one local function used by both the save button and IME Done. That function should trim the name, validate it, call `onSave(trimmed)`, then immediately call `onDone()`.
4. Keep `rememberSaveable` input state, focus request, placeholder, duplicate error, and active-sound pills, but switch all hardcoded dialog colors to `MaterialTheme.colorScheme` token mappings.
5. Update button styling to match FocusRitual's current design rules: no ripple, 0.5dp border, subdued primary enabled state, dim disabled state, and secondary cancel styling.
6. Add local press-scale feedback only if it can be done cleanly within the existing custom button boxes; otherwise preserve the current no-ripple interaction and leave motion limited to color/border animation.
7. Clean up unused imports and constants so the file reflects a single-state dialog with no residual saved-step code.

## Validation

- Run `./gradlew :composeApp:compileDebugKotlin`.
- Run `./gradlew :composeApp:compileKotlinIosSimulatorArm64`.
- Manually smoke-check the dialog from the existing current-mix flow:
  - open Current Mix
  - open Save Mix dialog
  - verify the text field autofocuses
  - verify blank and duplicate names keep save disabled
  - verify IME Done and the Save button both save and close immediately
  - verify there is no saved confirmation crossfade, rings, caption, or Done button
  - verify scrim tap and Cancel still dismiss without saving
