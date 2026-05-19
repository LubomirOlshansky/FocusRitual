# Focus Session Sheet — Readability & Header Card Redesign

## 1. Goal

Redesign the Focus Session configuration sheet to (a) introduce a prominent **session-type header card** that visually anchors the current mode (Focus vs Sleep) with an icon, title, and subtitle, and (b) improve **overall readability** of the sheet — tightening typography, alpha contrast, segmented control polish, and replacing the inline preset rows with a reusable, ripple-free `RitualRadioRow` component. All changes stay within the design system's token system; no new colors are introduced.

## 2. Scope & Non-goals

**In scope**
- `FocusSessionScreen` configuration sheet (Focus + Sleep modes).
- Header card above the segmented toggle.
- Radio row visual + interaction redesign.
- `SessionModeToggle` typography and alpha adjustments.
- Dynamic START button label per mode.
- New strings for header subtitles and START labels.

**Non-goals**
- No changes to `FocusSessionUiState` / `FocusSessionIntent` (MVI contract unchanged).
- No new color tokens or theme entries.
- No changes to Active Session screen, Mixer, Live Activity, or the Protect Focus card internals (only its surrounding card spacing may shift incidentally).
- No locale file changes outside the KMP compose resources directory.

## 3. Design-system mapping table

All `Color.White.copy(alpha = X)` from the user spec translate to `MaterialTheme.colorScheme.onSurface.copy(alpha = X)` with the **same alpha**. Background `#0c0e11` → existing theme surface token (do not introduce a literal).

| Spec element                            | Spec value                       | Token replacement                                              |
|-----------------------------------------|----------------------------------|----------------------------------------------------------------|
| Header card background                  | `Color.White.copy(0.06f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)`      |
| Header card border (0.5.dp)             | `Color.White.copy(0.10f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)`      |
| Header card icon tint                   | `Color.White.copy(0.65f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)`      |
| Header card title                       | `Color.White.copy(0.85f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)`      |
| Header card subtitle                    | `Color.White.copy(0.38f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)`      |
| Segmented toggle: active pill bg        | `Color.White.copy(0.10f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)`      |
| Segmented toggle: active text           | `Color.White.copy(0.90f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)`      |
| Segmented toggle: inactive text         | `Color.White.copy(0.30f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)`      |
| Radio row divider (0.5.dp)              | `Color.White.copy(0.06f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)`      |
| Radio circle border — unselected        | `Color.White.copy(0.28f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)`      |
| Radio circle border — selected          | `Color.White.copy(0.85f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)`      |
| Radio inner dot fill                    | `Color.White.copy(0.85f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)`      |
| Radio label — selected                  | `Color.White.copy(0.88f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)`      |
| Radio label — unselected                | `Color.White.copy(0.42f)`        | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)`      |

Where the existing file already uses `surfaceContainer*` family for translucent surfaces (see `FocusSessionScreen.kt` lines 252, 306), match that pattern instead of introducing a parallel `onSurface.copy(...)` background. Default rule: **surfaces** → `surfaceContainer*`; **strokes / glyph alphas** → `onSurface.copy(alpha = …)`.

## 4. File changes — concrete list

### NEW: `composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/RitualRadioRow.kt`

Reusable radio row used by both preset rows and the CUSTOM option.

**Signature**
```kotlin
@Composable
fun RitualRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Implementation notes**
- Row: `fillMaxWidth()`, `padding(vertical = 13.dp)`, `clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }`.
- Radio: 20.dp `Box` (or `Canvas`) with 1.5.dp border.
  - Border color animates between unselected `0.28f` and selected `0.85f` via `animateColorAsState(tween(200))`.
  - When `selected`, an inner 10.dp dot filled at `onSurface.copy(alpha = 0.85f)`.
- Label: 14.sp, `FontWeight.Light` (300) when unselected, `FontWeight.Normal` (400) when selected.
  - Color animates between `0.42f` and `0.88f` via `animateColorAsState(tween(200))`.
- Horizontal gap between radio and label: 12.dp.
- No ripple, no scale press feedback required (this is a radio row, not a primary action).

### NEW: `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/ui/SessionTypeHeaderCard.kt`

Encapsulates the mode → (icon, title, subtitle) mapping so callers only pass `mode`.

**Signature**
```kotlin
@Composable
fun SessionTypeHeaderCard(
    mode: SessionMode,
    modifier: Modifier = Modifier,
)
```

**Implementation notes**
- Outer `Box` / `Row`: `fillMaxWidth()`, `clip(RoundedCornerShape(12.dp))`, background `onSurface.copy(alpha = 0.06f)` (or matching `surfaceContainer*` if already in use), 0.5.dp border at `onSurface.copy(alpha = 0.10f)`, inner padding 12.dp.
- Layout: horizontal `Row` — leading icon (24.dp), 12.dp gap, `Column` { title, 2.dp gap, subtitle }.
- Crossfade(`tween(300)`) keyed on `mode` wraps the icon + text column so all three elements swap together.
- Mapping:
  - `SessionMode.FOCUS` → `Icons.Outlined.Timer`, title `session_focus_title` ("FOCUS SESSION"), subtitle `session_focus_subtitle` ("Timed work with breaks").
  - `SessionMode.SLEEP` → `Icons.Outlined.Bedtime`, title `session_sleep_title` ("SLEEP SESSION"), subtitle `session_sleep_subtitle` ("Sounds fade out over time").
- Title: 12.sp, `FontWeight.Medium`, letterSpacing 0.12.em, color `onSurface.copy(0.85f)`.
- Subtitle: 12.sp, `FontWeight.Light`, color `onSurface.copy(0.38f)`.
- Icon tint: `onSurface.copy(0.65f)`.

### EDIT: `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/FocusSessionScreen.kt`

- **Remove** the plain `Text("FOCUS SESSION" / "SLEEP SESSION")` block at lines 140–147. The top header row keeps only the `CloseButton` (`×`) aligned to its existing position.
- **Insert** `SessionTypeHeaderCard(mode = mode)` directly above `SessionModeToggle`, with:
  - 12.dp spacing between the close-button row and the header card.
  - 14.dp spacing between header card and `SessionModeToggle`.
  - 16.dp spacing between `SessionModeToggle` and the radio list.
- **Replace** inline `PresetRow` (407–445) usages in `FocusModeContent` and `SleepModeContent` with `RitualRadioRow(label = preset.label, selected = …, onClick = …)`. The `CUSTOM` option also uses `RitualRadioRow`.
  - After replacement, if `PresetRow` is no longer referenced anywhere, delete it. Verify with a quick reference search before deletion.
- **Add** a 0.5.dp horizontal divider at `onSurface.copy(alpha = 0.06f)` between radio rows (e.g., `HorizontalDivider` between items, not above the first or below the last).
- **Wire** `StartSessionButton(label = …)` to mode-derived string: `if (mode == SessionMode.FOCUS) stringResource(Res.string.start_focus_session) else stringResource(Res.string.start_sleep_session)`.

### EDIT: `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/ui/SessionModeToggle.kt`

- Active pill background → `onSurface.copy(alpha = 0.10f)` (down from `surfaceBright.copy(alpha = 0.85f)`).
- Active text color → `onSurface.copy(alpha = 0.90f)`.
- Inactive text color → `onSurface.copy(alpha = 0.30f)`.
- Label typography: 11.sp, letterSpacing 0.10.em, `FontWeight.Medium`.
- Preserve existing `tween(250)` color animations exactly as-is — just change the target color values.

### EDIT: `composeApp/src/commonMain/composeResources/values/strings.xml`

Add:
- `start_focus_session` = "START FOCUS SESSION"
- `start_sleep_session` = "START SLEEP SESSION"
- `session_focus_subtitle` = "Timed work with breaks"
- `session_sleep_subtitle` = "Sounds fade out over time"

Reuse existing `session_focus_title` / `session_sleep_title` for the header card titles. **Locale variants under `iosApp/iosApp/*.lproj` are NOT touched** — they are SwiftUI strings outside the KMP resource pipeline. KMP-side localized variants (if any exist under `composeApp/src/commonMain/composeResources/values-*`) should receive the same keys; English-only initially is acceptable if no other locale files exist yet — flag to Architect if uncertain.

## 5. Spacing implementation (concrete)

| Element                                           | Modifier                                |
|---------------------------------------------------|-----------------------------------------|
| Close-button row → header card                    | `Spacer(Modifier.height(12.dp))`        |
| Header card inner padding                         | `Modifier.padding(12.dp)`               |
| Header card title → subtitle                      | `Spacer(Modifier.height(2.dp))`         |
| Header card → SessionModeToggle                   | `Spacer(Modifier.height(14.dp))`        |
| SessionModeToggle → radio list                    | `Spacer(Modifier.height(16.dp))`        |
| Radio row vertical padding                        | `Modifier.padding(vertical = 13.dp)`    |
| Radio icon → label gap                            | `Spacer(Modifier.width(12.dp))`         |
| Radio divider thickness                           | 0.5.dp at `onSurface.copy(alpha=0.06f)` |

## 6. Animations

| Element                               | Spec                              | Syncs with                                              |
|---------------------------------------|-----------------------------------|---------------------------------------------------------|
| Header card icon + title + subtitle   | `Crossfade(tween(300))`           | Body `Crossfade(tween(350))` between Focus/Sleep layouts |
| Body Focus ↔ Sleep layout             | `Crossfade(tween(350))` (existing) | — (unchanged)                                            |
| Segmented toggle color transitions    | `tween(250)` (existing)           | — (unchanged)                                            |
| Radio border + label color            | `animateColorAsState(tween(200))` | Independent; faster than mode transitions for snappy feedback |
| START button label change             | Implicit via mode state           | Driven by same `mode` Crossfade trigger                  |

The 300ms header card crossfade is intentionally 50ms shorter than the body crossfade so the new mode's header settles slightly before the layout completes — gives a perceived lead from the header.

## 7. SOLID notes

- **SRP** — `RitualRadioRow` owns radio visuals + interaction only; `SessionTypeHeaderCard` owns mode→(icon, title, subtitle) mapping + visual presentation only. Neither knows about the other or about `FocusSessionScreen`'s layout concerns.
- **OCP** — `RitualRadioRow` accepts `label` as a parameter, so any future row (sound presets, durations, custom modes) reuses it without modification.
- **DIP** — `SessionTypeHeaderCard` depends on the abstract `SessionMode` enum, not on `FocusSessionViewModel` or any state holder. `FocusSessionScreen` injects the current `mode` from its hoisted local state.
- The header card encapsulates the icon/text mapping so `FocusSessionScreen` stays declarative: `SessionTypeHeaderCard(mode = mode)` — no branching in the parent.

## 8. Acceptance criteria

- [ ] Header card visible on both FOCUS and SLEEP, swaps icon + title + subtitle smoothly.
- [ ] Plain "FOCUS SESSION" / "SLEEP SESSION" text removed from header row.
- [ ] START button label reflects mode ("START FOCUS SESSION" / "START SLEEP SESSION").
- [ ] Radio rows: 20.dp circle, no ripple, 13.dp vertical padding, hairline divider between rows.
- [ ] No `Color.White` or `Color(0xFF…)` literals introduced anywhere in the touched files.
- [ ] All colors flow through `MaterialTheme.colorScheme.*` tokens.
- [ ] No changes to `FocusSessionUiState` / `FocusSessionIntent`.
- [ ] Android build passes; iOS framework assembles.
- [ ] Existing `Crossfade(tween(350))` between Focus/Sleep layouts is unchanged.
- [ ] Segmented toggle animations remain `tween(250)`.

## 9. Out of scope

- Active Session screen (`feature/timer/`).
- Mixer screen and `SoundMixer` behavior.
- Live Activity / widget extension.
- `ProtectFocusCard` internals — only its containing card's outer spacing may shift if the surrounding column padding changes; do not modify the component itself.
- Theme / color token changes.

## 10. Open questions

None — all resolved with user. If any surface during implementation (e.g., a missing localized strings file, an unexpected reference to `PresetRow` elsewhere), flag to Architect before proceeding.

## 11. Implementation order

1. Create `core/designsystem/component/RitualRadioRow.kt`.
2. Create `feature/session/ui/SessionTypeHeaderCard.kt`.
3. Add the four new strings to `composeApp/src/commonMain/composeResources/values/strings.xml`.
4. Wire `SessionTypeHeaderCard`, `RitualRadioRow`, dynamic START label, and updated spacing into `FocusSessionScreen.kt`; remove the plain mode label; delete unused `PresetRow` after reference check.
5. Adjust `SessionModeToggle.kt` colors and typography (animations untouched).
6. Build (Android debug + iOS framework) and visual check both modes, both Crossfade transitions, and the radio interaction.
