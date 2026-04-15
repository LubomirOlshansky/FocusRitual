# FocusRitual — Design System v2

## Creative Direction: "The Digital Sanctuary"
Dark cinematic ambient focus app. Premium, atmospheric, minimal.

## Color Tokens (Dark theme only)
### Surfaces
Surface=#0c0e11(surface), SurfaceContainerLowest=#000000, SurfaceContainerLow=#111418, SurfaceContainer=#161a1f, SurfaceContainerHigh=#1b2027, SurfaceContainerHighest=#20262e, SurfaceBright=#2a3240

### Primary
Primary=#b7c8db(primary), PrimaryContainer=#384858(primaryContainer), PrimaryDim=#a9bbcd(primaryFixed)

### Content
OnSurface=#e0e6f1(onSurface), OnSurfaceVariant=#a5abb6(onSurfaceVariant), OutlineVariant=#424851(outlineVariant), Outline=#707680(outline)

### Accent
Tertiary=#fff8f4(tertiary), TertiaryFixedDim=#efe0d0(not wired), SecondaryFixedDim=#8a9bae(secondaryFixed)

### Scheme mapping fix
primaryFixed=PrimaryDim, secondaryFixed=SecondaryFixedDim — so feature code never imports Color.kt

## Depth System
Layer 0=surface(bg), 1=surfaceContainer(inactive cards), 2=surfaceContainerHigh(active cards), 3=surfaceContainerHighest+8dp shadow(floating), 4=surfaceBright+16dp shadow(overlay). Never skip >1 layer.

## Core Rules
1. No Pure White (use onSurface or tertiary)
2. No Hard Borders (ghost borders: outlineVariant ≤15% alpha)
3. Glass Effect (surfaceBright@60% + layered alpha)
4. Signature Gradient (primary→primaryContainer 135°)
5. 300ms tween for all state changes
6. Ambient shadows for floating elements
7. No Ripple (indication=null)
8. No Divider Lines (spacing ≥7dp)
9. Theme Tokens Only in feature code

## Typography
displayLarge: 56sp Light 300 (scene name)
displayMedium: 64sp Light 300, tabular nums (timer countdown)
headlineSmall: 24sp Normal 400 (section headers)
titleLarge: 22sp Medium 500 (card titles)
bodyMedium: 15sp Light 300, -0.01em (sound names)
labelMedium: 11sp Normal 400, 0.14em (phase labels)
labelSmall: 10sp Normal 400, 0.12em (category labels, badges)

Weight rules: Light+Normal for mixer UI. Medium for titleLarge only. SemiBold for Hero button only. Never Bold.

## Component Patterns
- **Sound Tile:** surfaceContainerHigh@0.92(active)/surfaceContainer@0.72(inactive), 16dp corners, 0.5dp ghost border(outlineVariant@0.15/0.09), 34dp icon container, name=bodyMedium Light@0.82/0.42, VolumeSlider, 3-state organic icon(0.75/0.30/0f)
- **Category Pills:** 30dp height, 15dp corners, surfaceBright@0.55(active)/transparent(inactive), onSurface@0.88/onSurfaceVariant@0.55 text, 4dp primaryFixed dot, 0.5dp border
- **Section Headers:** labelSmall uppercase, onSurfaceVariant@0.30, count badge surfaceContainer@0.50
- **PlayButton:** 96dp circle, glass surfaceBright, 3 breathing rings, GlowColor radial gradient, 24dp shadow
- **VolumeSlider:** primary→primaryFixed gradient track, 4dp height, 14dp secondaryFixed thumb, spring organic motion(0.75/200)
- **Floating Panel:** 18dp corners, surfaceContainerHighest 96→93%, outlineVariant@0.12, 8dp shadow

## Spacing (Spacing object in core/designsystem/theme/Spacing.kt)
xs=4dp, sm=8dp, md=12dp, lg=16dp, xl=24dp, xxl=32dp
screenHorizontal=24dp, heroTopPadding=120dp, listBottomPadding=140dp
tileHorizontal=14dp, tileTop=13dp, tileBottom=12dp, tileBottomMargin=7dp
sectionHeaderTop=14dp, sectionHeaderBottom=8dp
pillRowVertical=12dp, pillSpacing=6dp, pillHorizontal=14dp