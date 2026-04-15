# FocusRitual — Design System v2

## Creative Direction: "The Digital Sanctuary"

A high-end, dimly lit listening room. Intentional asymmetry, tonal depth.
The screen is a canvas of light and shadow, not a collection of boxes.
Dark, atmospheric, premium, minimal.

---

## Color Tokens

Dark theme only. All tokens are available via `MaterialTheme.colorScheme.*` in composable code.

### Surfaces

| Token | Kotlin Const | Hex | `MaterialTheme.colorScheme.*` | Usage |
|-------|-------------|-----|-------------------------------|-------|
| surface | `Surface` | `#0c0e11` | `.surface` | Base layer, screen background |
| surface_container_lowest | `SurfaceContainerLowest` | `#000000` | `.surfaceContainerLowest` | Recessed elements |
| surface_container_low | `SurfaceContainerLow` | `#111418` | `.surfaceContainerLow` | Secondary sections |
| surface_container | `SurfaceContainer` | `#161a1f` | `.surfaceContainer` | Content areas, inactive cards |
| surface_container_high | `SurfaceContainerHigh` | `#1b2027` | `.surfaceContainerHigh` | Active card backgrounds |
| surface_container_highest | `SurfaceContainerHighest` | `#20262e` | `.surfaceContainerHighest` | Floating elements |
| surface_bright | `SurfaceBright` | `#2a3240` | `.surfaceBright` | Critical interactive elements, glass bg |
| scrim | `Scrim` | `#0c0e11` | `.scrim` | Behind pills over background image (at varying alpha) |

### Primary

| Token | Kotlin Const | Hex | `MaterialTheme.colorScheme.*` | Usage |
|-------|-------------|-----|-------------------------------|-------|
| primary | `Primary` | `#b7c8db` | `.primary` | Main CTA, active states, organic motion icon |
| primary_container | `PrimaryContainer` | `#384858` | `.primaryContainer` | Active card backgrounds, toggle track |
| primary_dim | `PrimaryDim` | `#a9bbcd` | `.primaryFixed` | Glow effects, gradient endpoints, pill dot |

### Content

| Token | Kotlin Const | Hex | `MaterialTheme.colorScheme.*` | Usage |
|-------|-------------|-----|-------------------------------|-------|
| on_surface | `OnSurface` | `#e0e6f1` | `.onSurface` | Primary text (warm off-white, **NEVER #ffffff**) |
| on_surface_variant | `OnSurfaceVariant` | `#a5abb6` | `.onSurfaceVariant` | Secondary text, subtitles, dimmed labels |
| outline_variant | `OutlineVariant` | `#424851` | `.outlineVariant` | Ghost borders (at **≤25% opacity**, never 100%) |
| outline | `Outline` | `#707680` | `.outline` | Structural outlines (rare), sleep phase color |

### Accent

| Token | Kotlin Const | Hex | `MaterialTheme.colorScheme.*` | Usage |
|-------|-------------|-----|-------------------------------|-------|
| tertiary | `Tertiary` | `#fff8f4` | `.tertiary` | Warmest white allowed |
| tertiary_fixed_dim | `TertiaryFixedDim` | `#efe0d0` | *(design system only)* | Ambient pulse (10% opacity) |
| secondary_fixed_dim | `SecondaryFixedDim` | `#8a9bae` | `.secondaryFixed` | Slider thumb, break phase color |

### Semantic aliases (same hex values, named for clarity in code)

```kotlin
// These are NOT separate Color.kt constants — use the scheme key directly
// Toggle:     primaryContainer (track), secondaryFixed (thumb)
// Organic:    primary (icon tint)
// Pill dot:   primaryFixed (#a9bbcd)
// Phase:      primary (focus), secondaryFixed (break), outline (sleep)
```

---

## Depth System

| Layer | Element | Surface token | Shadow |
|-------|---------|---------------|--------|
| 0 — Base | Screen background | `surface` (#0c0e11) | none |
| 1 — Content | Inactive sound cards | `surfaceContainer` (#161a1f) | none |
| 2 — Active content | Active sound cards | `surfaceContainerHigh` (#1b2027) | none |
| 3 — Floating | Bottom panel, modals | `surfaceContainerHighest` (#20262e) | `8.dp` |
| 4 — Overlay | Sheets, dialogs | `surfaceBright` (#2a3240) | `16.dp` |

**Rule:** Never skip more than one layer. A card on `surface` uses `surfaceContainer`, not `surfaceContainerHighest`. The visual depth reads as atmosphere, not as contrast noise.

---

## Typography

Defined in `Type.kt` as `FocusRitualTypography`. Access via `MaterialTheme.typography.*`.

| Style | `MaterialTheme.typography.*` | Size | Weight | Letter Spacing | Usage |
|-------|------------------------------|------|--------|----------------|-------|
| Display Large | `.displayLarge` | 56sp | Light (300) | default | Scene name |
| Display Medium | `.displayMedium` | 64sp | Light (300) | -0.03em | Timer countdown (tabular nums via `fontFeatureSettings = "tnum"`) |
| Headline Small | `.headlineSmall` | 24sp | Normal (400) | default | Section headers |
| Title Large | `.titleLarge` | 22sp | Medium (500) | default | Card titles |
| Body Medium | `.bodyMedium` | 15sp | Light (300) | -0.01em | Sound names, general text |
| Label Medium | `.labelMedium` | 11sp | Normal (400) | 0.14em | Phase labels ("FOCUS SESSION", "BREAK") |
| Label Small | `.labelSmall` | 10sp | Normal (400) | 0.12em | Category labels, badges, technical labels |

**Font:** System default (`FontFamily.Default`). Manrope planned — add via `composeResources/font/` later.

### Weight Rule Table

| Context | Weight | Style token |
|---------|--------|-------------|
| Timer countdown | Light 300 | `displayMedium` |
| Session hero button label | SemiBold 600 | `titleLarge` |
| Card title | Medium 500 | `titleLarge` |
| Sound name (active) | Light 300 | `bodyMedium` |
| Category pill text | Normal 400 | `labelSmall` |
| Section header | Normal 400 | `labelSmall` |
| Bottom panel label | Normal 400 | `labelSmall` |
| Volume value readout | Normal 400 | `labelSmall` |

**Rules:**
- **Only use Light (300) and Normal (400)** for ambient/mixer UI
- Medium (500) reserved for `titleLarge` only
- SemiBold (600) used **only** in the Hero Session Button label
- **Never use Bold (700)** anywhere in the app

---

## Core Rules

### 1. No Pure White
Never use `Color.White` or `#ffffff`. Use `onSurface` (`#e0e6f1`) for primary text or `tertiary` (`#fff8f4`) for warmest accent white.

### 2. No Hard Borders
Avoid `1.dp`+ solid borders. Use **ghost borders**: `outlineVariant` at ≤25% opacity. Or use **tonal surface shifts** (stepping up/down the surface container scale) instead of borders.

### 3. Glass Effect
`surfaceBright` at 60% opacity + layered alpha. No native backdrop-blur in Compose — fake it with layered semi-transparent surfaces.

### 4. Signature Gradient
`primary` → `primaryContainer` at 135° for active CTAs.

### 5. 300ms Transitions
All state changes use `tween(300)` with default easing. No springs for UI state changes — `tween` with `FastOutSlowInEasing` feels more premium.

### 6. Ambient Shadows
Floating elements: `shadow(8.dp, shape)` or `0px 24px 48px rgba(0,0,0,0.4)`.

### 7. No Ripple
Remove all ripple indications for a premium feel:
```kotlin
.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
) { ... }
```

### 8. No Divider Lines
Separate list items with spacing (≥7dp), never with divider lines.

### 9. Generous Void
Use generous spacing — 112dp+ page margins for cinematic scale. Let content breathe.

### 10. Theme Tokens Only in Feature Code
Feature code (`feature/*`) must use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*`. Never import `Color.kt` constants directly. Only design system component code (`core/designsystem/*`) may reference `Color.kt` constants directly.

---

## Component Patterns

### ImmersiveBackground
- Full-screen dark forest image (`Res.drawable.background`) + vertical gradient overlay
- Gradient: `transparent → surface@0.6 → surface@0.92 → surface@1.0`
- Stops at 0.0, 0.35, 0.55, 1.0
- **Bottom scrim layer** for scrollable content legibility:
  ```kotlin
  // Sits at the bottom of the hero Box, above the gradient overlay
  Box(Modifier.fillMaxWidth().height(240.dp).align(Alignment.BottomCenter)
      .background(Brush.verticalGradient(
          listOf(Color.Transparent, surface@0.85, surface@1.0)
      )))
  ```

### SectionScrim
- Gradient that sits between the hero and the LazyColumn
- Prevents category pills from being illegible over the raw background image
- `32.dp` height, vertical gradient: `transparent → surface@0.90`
- Usage: `ImmersiveBackground()` → `SectionScrim()` → `LazyColumn { ... }`

### Hero Session Button (PlayButton)
- 96dp circle (actual button), 220dp outermost breathing ring
- Glass background: `surfaceBright` with animated alpha (0.6 idle, 0.8 playing)
- 3 breathing rings at staggered speeds (4000ms, 5200ms, 6800ms)
- Glow: `GlowColor (#B7C8DB)` = `Primary` value, radial gradient with ambient pulse
- Inner halo ring + outer glow aura
- Icon: `40.dp` play/pause, tint `onSurface`
- 24dp shadow, no ripple

### Sound Tile
- **Active card:** `surfaceContainerHigh.copy(alpha = 0.92f)`, `outlineVariant@0.15` border
- **Inactive card:** `surfaceContainer.copy(alpha = 0.72f)`, `outlineVariant@0.09` border
- Corners: `16.dp`, border width: `0.5.dp`
- **Icon container:** `34.dp` square, `10.dp` corners
  - Active: `surfaceContainerHighest@0.80` bg, `outlineVariant@0.12` border
  - Inactive: `surfaceContainer@0.60` bg, `outlineVariant@0.08` border
  - Tint: `onSurfaceVariant@0.70` (active) / `onSurfaceVariant@0.25` (inactive)
- **Sound name:** `15.sp`, `FontWeight.Light`, `onSurface@0.82` (active) / `onSurface@0.42` (inactive)
- **Toggle:** Material `Switch` (checkedThumb=primary, checkedTrack=surfaceBright)
- **VolumeSlider:** existing component with organic motion animation
- **Organic motion icon (3-state alpha):**
  ```
  OM on + sound active  → alpha 0.75 (bright)
  OM off + sound active → alpha 0.30 (hint)
  sound inactive        → alpha 0.0  (invisible)
  ```
  - Size: `18.dp`, tint: `primary`, use `Modifier.alpha()` (not if/else — prevents layout shifts)
  - `Icons.Filled.AutoAwesome`, clickable with no ripple
- All animations: `tween(300)`

### VolumeSlider
- **Active track:** horizontal gradient `primary` → `primaryFixed` (#b7c8db → #a9bbcd)
- **Inactive track:** `surfaceContainerHighest` (#20262e)
- **Track dimensions:** `4.dp` height, `4.dp` corner radius
- **Thumb:** `14.dp` circle, `secondaryFixed` (#8a9bae) color, `2.dp` shadow
- **Organic motion:** `spring(dampingRatio = 0.75f, stiffness = 200f)` animated live value
- **Organic accent color:** soft teal shift `OrganicAccent (#8EC5E2)` on track start when OM active, `tween(500)` transition

### Floating Bottom Panel (Current Mix)
- `RoundedCornerShape(18.dp)`
- Background: `surfaceContainerHighest` vertical gradient, 96%→93% alpha
- Ghost border: `outlineVariant@0.12`, `0.5.dp`
- Shadow: `8.dp`
- Play/pause + AirPlay buttons: `40.dp` circles, `onSurface@0.08` background
- Labels: `labelSmall` for "CURRENT MIX", `bodySmall` for summary

### Category Pills
- `LazyRow`, `spacedBy(6.dp)`, `horizontal padding = 12.dp`
- Pill: `30.dp` height, `15.dp` corner radius (full pill), `14.dp` horizontal padding
- **Active:** `surfaceBright@0.55` background, `outlineVariant@0.25` border, `onSurface@0.88` text, `4.dp` `primaryFixed` dot
- **Inactive:** `Color.Transparent` background, `outlineVariant@0.14` border, `onSurfaceVariant@0.55` text
- Font: `labelSmall`
- Animation: `animateColorAsState(tween(300))` for all color transitions
- No ripple

### Section Headers
- Category name: `labelSmall`, uppercase, `letterSpacing = 0.12.sp`, `onSurfaceVariant@0.30`
- Active count badge: `surfaceContainer@0.50` background, `10.dp` corners, `onSurfaceVariant@0.25` text

---

## Spacing Reference

All spacing values are defined in `Spacing.kt` (`core/designsystem/theme/Spacing`). Feature code should reference `Spacing.*` — never hardcode dp values.

| Constant | Value | Usage |
|----------|-------|-------|
| `Spacing.xs` | `4.dp` | Micro gaps |
| `Spacing.sm` | `8.dp` | Small gaps |
| `Spacing.md` | `12.dp` | Medium gaps |
| `Spacing.lg` | `16.dp` | Large gaps |
| `Spacing.xl` | `24.dp` | Extra large gaps |
| `Spacing.xxl` | `32.dp` | Section gaps |
| `Spacing.screenHorizontal` | `24.dp` | Screen horizontal padding |
| `Spacing.heroTopPadding` | `120.dp` | Hero top padding |
| `Spacing.listBottomPadding` | `140.dp` | LazyColumn bottom (room for floating panel) |
| `Spacing.tileHorizontal` | `14.dp` | Sound card horizontal padding |
| `Spacing.tileTop` | `13.dp` | Sound card top padding |
| `Spacing.tileBottom` | `12.dp` | Sound card bottom padding |
| `Spacing.tileBottomMargin` | `7.dp` | Space between cards (no dividers) |
| `Spacing.sectionHeaderTop` | `14.dp` | Section header top padding |
| `Spacing.sectionHeaderBottom` | `8.dp` | Section header bottom padding |
| `Spacing.pillRowVertical` | `12.dp` | Category pill row vertical padding |
| `Spacing.pillSpacing` | `6.dp` | Pill-to-pill spacing |
| `Spacing.pillHorizontal` | `14.dp` | Pill internal horizontal padding |

---

## Animation Reference

| Animation | Spec |
|-----------|------|
| State change (color, alpha) | `animateColorAsState(tween(300))` |
| Float state change | `animateFloatAsState(tween(300))` |
| Organic motion volume | `spring(dampingRatio = 0.75f, stiffness = 200f)` |
| Organic accent color | `tween(500)` |
| Hero breathing | `infiniteRepeatable(tween(4000), RepeatMode.Reverse)` |
| Panel enter | `fadeIn(tween(400)) + slideInVertically(tween(400))` |
| Panel exit | `fadeOut(tween(300)) + slideOutVertically(tween(300))` |
| Press scale | `tween(150)` or `tween(200)` |
| Screen transitions | `Crossfade` with `tween(300)` |

---

## Code Files

| File | Path | Contents |
|------|------|----------|
| Color.kt | `core/designsystem/theme/Color.kt` | All color constants + `Scrim` token |
| Type.kt | `core/designsystem/theme/Type.kt` | `FocusRitualTypography` with 7 styles |
| Theme.kt | `core/designsystem/theme/Theme.kt` | `FocusRitualTheme`, `darkColorScheme` with `primaryFixed`/`secondaryFixed` wired |
| Spacing.kt | `core/designsystem/theme/Spacing.kt` | `Spacing` object with all dp constants |
| SoundTile.kt | `core/designsystem/component/SoundTile.kt` | Sound card + `SoundIcon.toImageVector()` |
| VolumeSlider.kt | `core/designsystem/component/VolumeSlider.kt` | Custom slider with organic motion |
| PlayButton.kt | `core/designsystem/component/PlayButton.kt` | Hero session button (breathing circle) |
