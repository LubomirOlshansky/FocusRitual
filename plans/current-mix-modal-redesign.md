# CurrentMixModal Redesign — Implementation Plan

> **Type:** Visual-only redesign. No ViewModel, audio, or organic-motion-engine changes.
> **Status:** Ready for Developer agent execution.
> **Related:** [docs/mixer-screen-refactor.md](../docs/mixer-screen-refactor.md), [plans/mixer-refactor.md](mixer-refactor.md)
> **Design system source of truth:** Serena memory `design_system`

---

## Scope & Constraints

### What changes
- `CurrentMixModal.kt` — full rewrite of composable body (scrim layer, layout, animation specs)
- `ui/modal/ModalHeader.kt` — full rewrite (sizes, tokens, CloseButton reuse, Crossfade)
- `ui/modal/ActiveSoundRow.kt` — full rewrite (card bg, border, sparkle, remove, top highlight)
- `ui/modal/GlobalOrganicMotionRow.kt` — full rewrite (compact row, state text, switch colors)
- `ui/modal/DoneButton.kt` — full rewrite (dark fill, new tokens) + add `SaveMixButton` composable
- `MixerContract.kt` — add `OpenSaveDialog` intent variant
- `MixerScreen.kt` — update `CurrentMixModal` call site (pass new `organicMotionSummary` param)
- `ui/modal/CircleIconButton.kt` — **NEW** extracted reusable circle-icon-button composable
- `ui/modal/LuxuryCardSurface.kt` — **NEW** extracted reusable luxury card surface composable

### What must NOT change
- `VolumeSlider.kt` — do not modify (including organic motion drift ghost band)
- `CloseButton.kt` — do not modify (reuse as-is via import)
- `MixerViewModel.kt` — no changes
- `OrganicMotionEngine.kt` — no changes
- `SoundState.kt` — no changes
- Audio layer — no changes
- Any file outside `feature/mixer/` except the reused `CloseButton` import

### Patterns that MUST survive
1. **Auto-dismiss** — `LaunchedEffect(activeSounds.isEmpty()) { if (activeSounds.isEmpty()) onDismiss() }` in `CurrentMixModal`
2. **Lambda hoisting** — `remember(sound.id, onIntent) { … }` for volume/organic/remove in `LazyColumn` items
3. **`animateItem()`** — on each `LazyColumn` item for add/remove animation
4. **Intent wiring** — all user actions fire existing `MixerIntent` variants (+ new `OpenSaveDialog`)
5. **`isVisible` gate** — `AnimatedVisibility(visible = isVisible && activeSounds.isNotEmpty(), …)`
6. **Stateful/stateless split** — modal stays a composable in feature root, sub-composables stay `internal` in `ui/modal/`

---

## Style Rules (global to all files)

```
No Color.White — use onSurface only
No Color.Black — use surfaceContainerLowest for scrim
No ripple — indication = null on every clickable
No bold/medium weight — Light(300) and Normal(400) only
Borders 0.5dp everywhere (fixes current 1dp in ActiveSoundRow)
Sound card bg = Color(0xFF191E25) — documented exception (add to design_system)
Done btn bg = Color(0xFF16171B) — documented exception (add to design_system)
All other colors via MaterialTheme.colorScheme.*
animateFloatAsState(tween(300)) for sparkle alpha
Crossfade(tween(250)) for pause/play icon swap
Reuse CloseButton from core/designsystem/component/CloseButton.kt
Reuse VolumeSlider as-is
FocusRitualEasing.DeepEaseOut for entry, FocusRitualEasing.CinematicIn for exit (not FastOutSlowInEasing)
```

---

## Step 1 — `MixerContract.kt`: Add `OpenSaveDialog` Intent

**File:** [MixerContract.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt)

### Current state
```kotlin
sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
    data class ToggleOrganicMotion(val soundId: String) : MixerIntent
    data class RemoveFromMix(val soundId: String) : MixerIntent
    data object ToggleGlobalOrganicMotion : MixerIntent
    data class SelectCategory(val category: SoundCategory) : MixerIntent
}
```

### Change
Add one line after `ToggleGlobalOrganicMotion`:
```kotlin
    data object OpenSaveDialog : MixerIntent
```

### Impact
- `MixerViewModel.onIntent()` will need a `when` branch for this — but that's a future feature task. For now the ViewModel will have an exhaustive-when compile error.
- **Fix:** Add a no-op branch in `MixerViewModel.onIntent()`:
  ```kotlin
  is MixerIntent.OpenSaveDialog -> { /* TODO: implement save dialog */ }
  ```
  Find the `when(intent)` block in `MixerViewModel.onIntent()` and add this branch.

---

## Step 2 — `ui/modal/ModalHeader.kt`: Full Rewrite

**File:** [ModalHeader.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/ModalHeader.kt)

### Current state
- Inline close button (36dp, 20dp icon, 0.6 alpha) — NOT using `CloseButton` component
- Title "CURRENT MIX" — `labelSmall`, `letterSpacing = 2.sp`, `onSurface@0.6`
- Subtitle — `bodySmall`, `onSurface@0.5`
- Play/pause — 40dp circle, `onSurface@0.08` bg, instant icon swap (no Crossfade), 20dp icon

### New signature
```kotlin
@Composable
internal fun ModalHeader(
    activeSoundCount: Int,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayback: () -> Unit,
)
```
(Signature unchanged — no caller impact.)

### New structure (pseudo-code)
```kotlin
Row(fillMaxWidth, padding(horizontal=20dp, vertical=16dp), verticalAlignment=CenterVertically) {

    // LEFT: reuse CloseButton
    CloseButton(onClick = onDismiss)
    // Import: com.focusritual.app.core.designsystem.component.CloseButton

    Spacer(weight(1f))

    // CENTER: title stack
    Column(horizontalAlignment = CenterHorizontally) {
        Text(
            text = "CURRENT MIX",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,     // W400
            letterSpacing = 0.14.em,
            color = colorScheme.onSurface.copy(alpha = 0.28f),
        )
        Text(
            text = "$activeSoundCount sounds active",
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,      // W300
            color = colorScheme.onSurface.copy(alpha = 0.46f),
        )
    }

    Spacer(weight(1f))

    // RIGHT: pause/play button
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(colorScheme.surfaceContainerHighest.copy(alpha = 0.90f))
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.20f), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null)
                { onTogglePlayback() },
        contentAlignment = Center,
    ) {
        Crossfade(
            targetState = isPlaying,
            animationSpec = tween(250),
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                tint = colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
```

### Key differences from current
| Aspect | Old | New |
|---|---|---|
| Close button | Inline 36dp, 20dp icon, 0.6 alpha | Reuse `CloseButton` (28dp, 0.5dp border, 14dp icon, 0.38 alpha) |
| Title "CURRENT MIX" | `labelSmall` (10sp), `letterSpacing = 2.sp`, onSurface@0.6 | 10sp Normal, `letterSpacing = 0.14.em`, onSurface@0.28 |
| Subtitle | `bodySmall`, onSurface@0.5 | 11sp Light, onSurface@0.46 |
| Play/pause size | 40dp circle | 28dp circle |
| Play/pause bg | onSurface@0.08 | surfaceContainerHighest@0.90 + border |
| Play/pause border | none | 0.5dp outlineVariant@0.20 |
| Icon swap | Instant (if/else) | `Crossfade(tween(250))` |
| Play/pause icon size | 20dp | 14dp |
| Play/pause icon alpha | 0.9 | 0.55 |

### New imports needed
```kotlin
import com.focusritual.app.core.designsystem.component.CloseButton
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
```

---

## Step 3 — `ui/modal/GlobalOrganicMotionRow.kt`: Full Rewrite

**File:** [GlobalOrganicMotionRow.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/GlobalOrganicMotionRow.kt)

### Current state
- Two-line text (title + "For all active sounds") — `bodyMedium`/`bodySmall`
- Material3 `Switch` with custom colors
- No icon, no summary text, no hairline divider

### New signature
```kotlin
@Composable
internal fun GlobalOrganicMotionRow(
    checked: Boolean,
    organicMotionSummary: String,   // NEW — e.g. "All sounds", "Rain only", "Off"
    allSoundsOrganic: Boolean,      // NEW — true when every active sound has organic on
    onToggle: () -> Unit,
)
```

### New structure (pseudo-code)
```kotlin
Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 10.dp),
        verticalAlignment = CenterVertically,
    ) {
        // Small icon — sparkle/organic indicator
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (checked)
                colorScheme.primary.copy(alpha = 0.42f)
            else
                colorScheme.outlineVariant.copy(alpha = 0.35f),
        )
        Spacer(Modifier.width(8.dp))

        // Label
        Text(
            text = "Organic motion",
            fontSize = 10.sp,
            fontWeight = FontWeight.Light,
            color = colorScheme.onSurface.copy(alpha = 0.28f),
        )
        Spacer(Modifier.width(8.dp))

        // State text — reads actual organic state
        Text(
            text = organicMotionSummary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Normal,
            color = if (organicMotionSummary == "Off")
                colorScheme.onSurface.copy(alpha = 0.20f)
            else
                colorScheme.primary.copy(alpha = 0.52f),
        )

        Spacer(Modifier.weight(1f))

        // Switch — scaled 0.85
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.85f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.onSurface.copy(alpha = 0.92f),
                checkedTrackColor = colorScheme.primaryContainer,
                uncheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.28f),
                uncheckedTrackColor = colorScheme.surfaceContainerHighest,
                // suppress default borders:
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }

    // Hairline divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(0.5.dp)
            .background(colorScheme.outlineVariant.copy(alpha = 0.08f)),
    )
}
```

### Key differences from current
| Aspect | Old | New |
|---|---|---|
| Layout | Row with Column(2 texts) + Switch | Row with icon + label + state text + switch, then hairline |
| Title | `bodyMedium` "Organic Motion" | 10sp Light "Organic motion" onSurface@0.28 |
| Subtitle | `bodySmall` "For all active sounds" | Dynamic state text: "All sounds" / "{Name} only" / "Off" |
| Icon | none | 14dp sparkle, primary@0.42 (on) / outlineVariant@0.35 (off) |
| Switch scale | 1.0 | 0.85 |
| Switch colors | primary thumb / surfaceBright track | onSurface@0.92 thumb / primaryContainer track (on); onSurface@0.28 thumb / surfaceContainerHighest track (off) |
| Divider | none | 0.5dp outlineVariant@0.08 below |

---

## Step 4 — `ui/modal/ActiveSoundRow.kt`: Full Rewrite

**File:** [ActiveSoundRow.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/ActiveSoundRow.kt)

### Current state
- `Surface` with 18dp corners, `surfaceContainerHigh` bg, **1dp** border at 0.25 alpha
- Flat row: icon (20dp) + name (`bodyMedium`) + organic sparkle (28dp, color only) + remove (28dp) + VolumeSlider
- No card bg override, no top highlight, no icon container

### New signature
```kotlin
@Composable
internal fun ActiveSoundRow(
    sound: SoundState,
    onAdjustVolume: (Float) -> Unit,
    onToggleOrganicMotion: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
)
```
(Unchanged — no caller impact.)

### New structure (pseudo-code)
```kotlin
Box(
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF191E25))     // documented exception
        .border(0.5.dp, Color(0xFFFFFFFF).copy(alpha = 0.07f), RoundedCornerShape(16.dp)),
) {
    // Top highlight overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colorScheme.primary.copy(alpha = 0.10f),
                        colorScheme.primary.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                ),
            ),
    )

    Column(
        modifier = Modifier.padding(horizontal = 13.dp).padding(top = 13.dp, bottom = 12.dp),
    ) {
        // TOP ROW
        Row(fillMaxWidth, verticalAlignment = CenterVertically) {

            // Icon container
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFFFFF).copy(alpha = 0.04f))
                    .border(0.5.dp, Color(0xFFFFFFFF).copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Center,
            ) {
                Icon(
                    imageVector = sound.icon,
                    contentDescription = sound.name,
                    modifier = Modifier.size(12.dp),
                    tint = colorScheme.onSurface.copy(alpha = 0.48f),
                )
            }

            Spacer(width = 10.dp)

            // Sound name
            Text(
                text = sound.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.01).em,
                color = colorScheme.onSurface.copy(alpha = 0.84f),
            )

            Spacer(weight(1f))

            // Sparkle — organic motion per-sound toggle (26dp circle)
            val sparkleAlpha by animateFloatAsState(
                targetValue = if (sound.organicMotion) 1f else 0f,
                animationSpec = tween(300),
            )
            val sparkleBg = if (sound.organicMotion)
                colorScheme.primary.copy(alpha = 0.10f)
            else
                colorScheme.surfaceContainerHighest.copy(alpha = 0.70f)
            val sparkleBorder = if (sound.organicMotion)
                colorScheme.primary.copy(alpha = 0.22f)
            else
                colorScheme.outlineVariant.copy(alpha = 0.18f)
            val sparkleIconTint = if (sound.organicMotion)
                colorScheme.primary.copy(alpha = 0.82f)
            else
                colorScheme.onSurface.copy(alpha = 0.20f)
            // Use sparkleAlpha to lerp between on/off states for smooth transition:
            // (This is conceptually: final = lerp(offColor, onColor, sparkleAlpha))
            // Simpler: just animate the discrete state since colors differ

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(sparkleBg)
                    .border(0.5.dp, sparkleBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onToggleOrganicMotion() },
                contentAlignment = Center,
            ) {
                // ✦ sparkle character or Icons.Filled.AutoAwesome
                Text(
                    text = "✦",
                    fontSize = 12.sp,
                    color = sparkleIconTint,
                )
                // ALTERNATIVE if ✦ doesn't render well on all platforms:
                // Icon(Icons.Filled.AutoAwesome, …, size=12.dp, tint=sparkleIconTint)
            }

            Spacer(width = 6.dp)

            // Remove button (26dp circle, ✕ icon)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceContainerHighest.copy(alpha = 0.60f))
                    .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.14f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onRemove() },
                contentAlignment = Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(10.dp),
                    tint = colorScheme.onSurface.copy(alpha = 0.22f),
                )
            }
        }

        // BOTTOM ROW: VolumeSlider — KEEP EXACTLY AS-IS
        VolumeSlider(
            value = sound.volume,
            onValueChange = onAdjustVolume,
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            liveValue = sound.liveVolume,
        )
    }
}
```

### Key differences from current
| Aspect | Old | New |
|---|---|---|
| Wrapper | `Surface(shape=18dp, surfaceContainerHigh)` | `Box(shape=16dp, Color(0xFF191E25))` |
| Border | **1dp** outlineVariant@0.25 | **0.5dp** Color(0xFFFFFFFF)@0.07 |
| Top highlight | none | 1dp horizontal gradient primary@0.10→0.05 |
| Padding | 16dp horizontal, 12dp vertical | 13dp horizontal, 13dp top, 12dp bottom |
| Icon | bare 20dp, onSurfaceVariant | 28dp container (8dp corners, 0.04 bg, 0.08 border), 12dp icon onSurface@0.48 |
| Name | `bodyMedium`, onSurface (full) | 16sp Light, letterSpacing=-0.01em, onSurface@0.84 |
| Sparkle toggle | 28dp bare icon, color-only animation | 26dp circle with bg/border, ✦ character, animated alpha |
| Sparkle on state | primary@0.70 | primary@0.10 bg, primary@0.22 border, primary@0.82 icon |
| Sparkle off state | onSurfaceVariant@0.18 | surfaceContainerHighest@0.70 bg, outlineVariant@0.18 border, onSurface@0.20 icon |
| Remove button | 28dp bare icon, onSurfaceVariant@0.4 | 26dp circle, surfaceContainerHighest@0.60 bg, outlineVariant@0.14 border, 10dp icon onSurface@0.22 |

### About `Color(0xFFFFFFFF)` in card
The spec uses `Color(0xFFFFFFFF).copy(alpha = 0.07f)` for the card border and icon container — this is intentionally white-at-low-alpha, NOT `onSurface`. This is acceptable here because it's part of the card's rich-dark treatment (same documented exception pattern as the card bg). `onSurface` (#e0e6f1) has a warm tint that would give a different look at these micro-alphas.

**Important:** The `Color.Transparent` used in the gradient is semantically Transparent, not Color.White or Color.Black. This is fine.

---

## Step 5 — `ui/modal/DoneButton.kt`: Rewrite + Add `SaveMixButton`

**File:** [DoneButton.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt)

### Current state
- Single `DoneButton` composable: pill, surfaceBright@0.50 bg, 0.5dp outlineVariant@0.12, `labelMedium` onSurface@0.85

### New: Two composables in same file

#### `SaveMixButton`
```kotlin
@Composable
internal fun SaveMixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Center,
    ) {
        Row(verticalAlignment = CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,  // or similar bookmark icon
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = colorScheme.onSurface.copy(alpha = 0.28f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Save this mix",
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = colorScheme.onSurface.copy(alpha = 0.38f),
            )
        }
    }
}
```

#### `DoneButton` (rewritten)
```kotlin
@Composable
internal fun DoneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF16171B).copy(alpha = 0.92f))   // documented exception
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        contentAlignment = Center,
    ) {
        // Inner top highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(colorScheme.onSurface.copy(alpha = 0.04f)),
        )
        Text(
            text = "Done",
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            color = colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}
```

### Key differences from current DoneButton
| Aspect | Old | New |
|---|---|---|
| Width | wrap content (horizontal padding 32dp) | `fillMaxWidth()`, 48dp height |
| Background | surfaceBright@0.50 | Color(0xFF16171B)@0.92 |
| Corner | 999dp (full pill) | 24dp (pill shape) |
| Border | 0.5dp outlineVariant@0.12 | 0.5dp outlineVariant@0.22 |
| Top highlight | none | 1dp onSurface@0.04 |
| Text | `labelMedium` (11sp Normal), onSurface@0.85 | 13sp Light, onSurface@0.55 |
| New | — | `SaveMixButton` composable added above |

---

## Step 6 — `CurrentMixModal.kt`: Full Rewrite

**File:** [CurrentMixModal.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt)

### Current state
- `AnimatedVisibility` wraps everything (no separate scrim)
- Background: `surface@0.98`
- Layout: `Column { ModalHeader → LazyColumn(GlobalOrganicMotionRow + items) }` with `DoneButton` floating at `BottomCenter`
- Enter: `fadeIn(350) + slideInVertically(400, FastOutSlowInEasing)`
- Exit: `fadeOut(250) + slideOutVertically(300, FastOutSlowInEasing)`

### New signature
```kotlin
@Composable
fun CurrentMixModal(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    isPlaying: Boolean,
    anyOrganicOn: Boolean,
    organicMotionSummary: String = "",     // NEW — derived in caller
    allSoundsOrganic: Boolean = false,     // NEW — derived in caller
    onIntent: (MixerIntent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)
```

New params have defaults so the existing MixerScreen call site continues to compile before being updated.

### New structure (pseudo-code)
```kotlin
@Composable
fun CurrentMixModal(…) {
    // Auto-dismiss (preserved exactly)
    LaunchedEffect(activeSounds.isEmpty()) {
        if (activeSounds.isEmpty()) onDismiss()
    }

    // === SCRIM LAYER ===
    AnimatedVisibility(
        visible = isVisible && activeSounds.isNotEmpty(),
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(250)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surfaceContainerLowest.copy(alpha = 0.60f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss() },
        )
    }

    // === CONTENT PANEL ===
    AnimatedVisibility(
        visible = isVisible && activeSounds.isNotEmpty(),
        enter = fadeIn(tween(350, easing = FocusRitualEasing.DeepEaseOut))
            + slideInVertically(tween(400, easing = FocusRitualEasing.DeepEaseOut)) { it },
        exit = fadeOut(tween(250, easing = FocusRitualEasing.CinematicIn))
            + slideOutVertically(tween(300, easing = FocusRitualEasing.CinematicIn)) { it },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. Header
                ModalHeader(
                    activeSoundCount = activeSounds.size,
                    isPlaying = isPlaying,
                    onDismiss = onDismiss,
                    onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
                )

                // 2. Scrollable area (organic row + sound cards)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp),
                ) {
                    item(key = "organic-motion") {
                        GlobalOrganicMotionRow(
                            checked = anyOrganicOn,
                            organicMotionSummary = organicMotionSummary,
                            allSoundsOrganic = allSoundsOrganic,
                            onToggle = { onIntent(MixerIntent.ToggleGlobalOrganicMotion) },
                        )
                    }

                    items(
                        items = activeSounds,
                        key = { it.id },
                    ) { sound ->
                        val onAdjustVolume = remember(sound.id, onIntent) {
                            { v: Float -> onIntent(MixerIntent.AdjustVolume(sound.id, v)) }
                        }
                        val onToggleOrganic = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.ToggleOrganicMotion(sound.id)) }
                        }
                        val onRemove = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.RemoveFromMix(sound.id)) }
                        }
                        ActiveSoundRow(
                            sound = sound,
                            onAdjustVolume = onAdjustVolume,
                            onToggleOrganicMotion = onToggleOrganic,
                            onRemove = onRemove,
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 8.dp),   // 8dp gap between cards
                        )
                    }
                }

                // 3. Bottom buttons — fixed at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    SaveMixButton(
                        onClick = { onIntent(MixerIntent.OpenSaveDialog) },
                    )
                    DoneButton(
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}
```

### Key differences from current
| Aspect | Old | New |
|---|---|---|
| Scrim | No separate scrim; single `surface@0.98` bg | Separate `AnimatedVisibility` scrim: `surfaceContainerLowest@0.60`, tap-to-dismiss |
| Background | `surface@0.98` | No background on content panel (transparent over scrim) |
| Enter easing | `FastOutSlowInEasing` | `FocusRitualEasing.DeepEaseOut` |
| Exit easing | `FastOutSlowInEasing` | `FocusRitualEasing.CinematicIn` |
| DoneButton position | `BottomCenter` overlay, `padding(bottom=24dp)` | Fixed below LazyColumn in parent Column |
| Save button | none | `SaveMixButton` above Done, fires `OpenSaveDialog` |
| LazyColumn bottom padding | 100dp (to clear floating Done) | 16dp (buttons are outside scroll) |
| Card gap | 4dp vertical padding per item | 8dp bottom padding per item |

### Important: Scrim architecture
The scrim is a **separate** `AnimatedVisibility` that fades independently. Content panel slides over it. Both share the same visibility condition. The scrim `Box` has `clickable → onDismiss()` to handle tap-to-dismiss. The content panel does NOT consume clicks (so tapping outside cards reaches the scrim).

**Consideration:** If the content panel covers the full screen, the scrim tap won't reach. Since the content is `fillMaxSize` with items, this is fine — tapping empty space in the LazyColumn won't reach the scrim either. The scrim is primarily visible during the slide-in/out animation. For tap-to-dismiss, the existing close button and done button serve as the primary dismiss affordances.

**Alternative (recommended):** Make the content panel NOT consume background clicks. Don't add a background color to the content panel — let it be transparent so the scrim shows through gaps. This means:
- The `Box` wrapping the content should NOT have `.background(…)` 
- The scrim's clickable handles dismiss for any area not covered by sound cards or buttons

---

## Step 7 — `MixerScreen.kt`: Update Caller

**File:** [MixerScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt)

### Current call site (lines ~198-205)
```kotlin
CurrentMixModal(
    isVisible = showCurrentMixModal,
    activeSounds = activeSounds,
    isPlaying = uiState.isPlaying,
    anyOrganicOn = anyOrganicOn,
    onIntent = onIntent,
    onDismiss = { showCurrentMixModal = false },
)
```

### New call site
Add derived values before the call and pass new params:

```kotlin
// Add these derived values near line 79 (after anyOrganicOn):
val allSoundsOrganic = remember(activeSounds) {
    activeSounds.isNotEmpty() && activeSounds.all { it.organicMotion }
}
val organicMotionSummary = remember(activeSounds) {
    val organicSounds = activeSounds.filter { it.organicMotion }
    when {
        organicSounds.isEmpty() -> "Off"
        organicSounds.size == activeSounds.size -> "All sounds"
        organicSounds.size == 1 -> "${organicSounds.first().name} only"
        else -> "${organicSounds.size} sounds"
    }
}

// Updated call:
CurrentMixModal(
    isVisible = showCurrentMixModal,
    activeSounds = activeSounds,
    isPlaying = uiState.isPlaying,
    anyOrganicOn = anyOrganicOn,
    organicMotionSummary = organicMotionSummary,
    allSoundsOrganic = allSoundsOrganic,
    onIntent = onIntent,
    onDismiss = { showCurrentMixModal = false },
)
```

### Edge case: organicMotionSummary when 2+ but not all have organic
The spec only mentions "All sounds" and "{SoundName} only". For the case where 2+ but not all sounds have organic on, we use `"${count} sounds"` as a reasonable fallback that follows the same visual style. This is a minor addendum the Developer should implement.

---

## Design System Compliance — Deviations Being Fixed

| # | Old (deviation) | New (compliant) | File |
|---|---|---|---|
| 1 | 1dp border on ActiveSoundRow | 0.5dp border | ActiveSoundRow.kt |
| 2 | Inline close button (36dp, custom) | Reuse `CloseButton` component (28dp) | ModalHeader.kt |
| 3 | `FastOutSlowInEasing` on enter/exit | `FocusRitualEasing.DeepEaseOut` / `CinematicIn` | CurrentMixModal.kt |
| 4 | No `Crossfade` on play/pause icon | `Crossfade(tween(250))` | ModalHeader.kt |
| 5 | `surface@0.98` background (too opaque) | `surfaceContainerLowest@0.60` scrim (separate layer) | CurrentMixModal.kt |
| 6 | Play/pause 40dp (competes with content) | 28dp (matches CloseButton scale) | ModalHeader.kt |
| 7 | Switch not scaled | `scale(0.85f)` | GlobalOrganicMotionRow.kt |
| 8 | No bookmark/save action | SaveMixButton + OpenSaveDialog intent | DoneButton.kt, MixerContract.kt |

### New documented exceptions
These hardcoded hex values are added to the design system's exception list:
- `Color(0xFF191E25)` — sound card background in CurrentMixModal (rich-dark card)
- `Color(0xFF16171B)` — Done button background (dark fill primary exit)
- `Color(0xFFFFFFFF).copy(alpha=…)` — used at micro-alphas (0.04–0.08) inside cards for highlight/border; intentionally white rather than onSurface for neutral tint at these scales

---

## Code Quality Improvements (Medium Effort)

Apply SOLID principles and composition patterns during the rewrite — medium effort, not a hard refactor.

### 1. Extract `CircleIconButton` composable → `ui/modal/CircleIconButton.kt` (NEW FILE)

The redesign has 3 instances of the same pattern: circle with background, border, icon, no ripple, `indication = null`.
- Play/pause in ModalHeader (28dp)
- Sparkle toggle in ActiveSoundRow (26dp)
- Remove button in ActiveSoundRow (26dp)

Create a small, focused composable:
```kotlin
@Composable
internal fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit,
)
```
This applies **DRY** and **composition over duplication**. The `content` slot allows any icon (Icon, Text("✦"), Crossfade wrapper).

### 2. Extract `organicMotionSummary()` pure function → in `MixerScreen.kt` (top-level)

Instead of inlining the when-expression inside `remember {}` in MixerScreen, extract it as a testable pure function:
```kotlin
internal fun organicMotionSummary(activeSounds: List<SoundState>): String {
    val organicSounds = activeSounds.filter { it.organicMotion }
    return when {
        organicSounds.isEmpty() -> "Off"
        organicSounds.size == activeSounds.size -> "All sounds"
        organicSounds.size == 1 -> "${organicSounds.first().name} only"
        else -> "${organicSounds.size} sounds"
    }
}
```
This applies **single responsibility** and makes the logic testable without a Compose runtime.

### 3. Extract `LuxuryCardSurface` composable → `ui/modal/LuxuryCardSurface.kt` (NEW FILE)

The sound card treatment (solid dark bg, 0.5dp white border, top highlight gradient, 16dp corners) is a reusable "luxury card" pattern that could appear in future modals (e.g., saved mixes). Rather than inlining all the styling in ActiveSoundRow, extract the container:
```kotlin
@Composable
internal fun LuxuryCardSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```
Encapsulates: `Color(0xFF191E25)` bg, `Color(0xFFFFFFFF)@0.07` border, `16.dp` corners, top highlight gradient. Content slot gets the inner padding.

This applies **single responsibility** (ActiveSoundRow focuses on layout/behavior, not surface styling) and **open/closed** (card styling can evolve without touching row logic).

### 4. Clean up `CurrentMixModal` composition

Split the modal body into two private composables within `CurrentMixModal.kt`:
- `private fun ModalScrim(visible, onDismiss)` — handles the scrim AnimatedVisibility + background + clickable
- `private fun ModalContent(...)` — handles the content AnimatedVisibility + Column layout

This keeps `CurrentMixModal` as a clean orchestrator (~30 lines) rather than a 100+ line monolith. Not a new file — just internal composition within the same file.

### 5. Parameter naming — be explicit

Rename `checked` → `isOrganicMotionEnabled` in GlobalOrganicMotionRow for clarity at call sites.

---

## Suggested Implementation Order

Execute steps sequentially. Each step should compile independently.

| Order | Step | File(s) |
|---|---|---|
| 1 | Add `OpenSaveDialog` intent + no-op branch | MixerContract.kt, MixerViewModel.kt |
| 2 | NEW: Create `CircleIconButton` | ui/modal/CircleIconButton.kt |
| 3 | NEW: Create `LuxuryCardSurface` | ui/modal/LuxuryCardSurface.kt |
| 4 | Rewrite `ModalHeader` (uses CircleIconButton) | ui/modal/ModalHeader.kt |
| 5 | Rewrite `GlobalOrganicMotionRow` | ui/modal/GlobalOrganicMotionRow.kt |
| 6 | Rewrite `ActiveSoundRow` (uses LuxuryCardSurface + CircleIconButton) | ui/modal/ActiveSoundRow.kt |
| 7 | Rewrite `DoneButton` + add `SaveMixButton` | ui/modal/DoneButton.kt |
| 8 | Rewrite `CurrentMixModal` (ModalScrim/ModalContent split) | CurrentMixModal.kt |
| 9 | Update caller + extract `organicMotionSummary()` | MixerScreen.kt |
| 10 | Build verification | — |

### Per-step build check
After each step, the project should compile. Steps 1–3 create new code (intent + new composables). Steps 4–7 are independent rewrites. Step 8 wires everything together (new params with defaults — backward compatible). Step 9 passes the actual values.

---

## Appendix: New Imports Summary

### ModalHeader.kt — add
```kotlin
import com.focusritual.app.core.designsystem.component.CloseButton
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
```

### ModalHeader.kt — remove
```kotlin
// Remove inline close button code (replaced by CloseButton import)
```

### GlobalOrganicMotionRow.kt — add
```kotlin
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
```

### ActiveSoundRow.kt — add
```kotlin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
```

### ActiveSoundRow.kt — remove
```kotlin
// Remove: Surface import (replaced by Box)
// Remove: borderAlpha animation (replaced by static 0.5dp border)
```

### DoneButton.kt — add
```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.BookmarkBorder  // or similar
```

### CurrentMixModal.kt — add
```kotlin
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.feature.mixer.ui.modal.SaveMixButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
```

### CurrentMixModal.kt — remove
```kotlin
// Remove: FastOutSlowInEasing import
```
