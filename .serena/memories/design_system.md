# FocusRitual — Design Language v1.0 (Source of Truth)

This memory is the canonical design language. Any visual decision must conform to it.

---

## 1. Creative Direction

**Concept:** A **digital sanctuary** — not a productivity tool, not a timer app. A protected, dimly-lit space.

**Feels like:** dim private room at night, fog in a forest, the quiet before something begins, expensive/restrained.
**Never feels like:** fitness tracker, bright meditation app, generic dark mode, technical audio tool.

**Three emotional states:**
| State | Screen | Feeling |
|---|---|---|
| Ambient | Mixer | Alive, atmospheric, exploratory |
| Intention | Session Setup | Focused, deliberate, calm |
| Ritual | Active Session | Immersive, minimal, protected |

---

## 2. Color System

**Philosophy:** No pure white, no accent colors, opacity is the language, ONE tint (`primary` blue-grey #b7c8db).

### Surface scale (dark→light) — never skip more than one step
| Token | Hex | Usage |
|---|---|---|
| `surface` | #0c0e11 | Screen background |
| `surfaceContainerLowest` | #000000 | Recessed/overlays |
| `surfaceContainerLow` | #111418 | Secondary sections |
| `surfaceContainer` | #161a1f | Inactive cards |
| `surfaceContainerHigh` | #1b2027 | Active cards, elevated |
| `surfaceContainerHighest` | #20262e | Floating panels, bottom bar |
| `surfaceBright` | #2a3240 | Mode toggle active tab, highest |

### Content
| Token | Hex | Usage |
|---|---|---|
| `onSurface` | #e0e6f1 | Primary text (warm off-white, NEVER #ffffff) |
| `onSurfaceVariant` | #a5abb6 | Secondary text, labels |
| `outlineVariant` | #424851 | Ghost borders (≤18% alpha) |
| `outline` | #707680 | Structural outlines (rare) |

### Primary accent (the only tint)
| Token | Hex | Usage |
|---|---|---|
| `primary` | #b7c8db | Active toggles, CTA, organic icon, active states |
| `primaryContainer` | #384858 | Toggle track when active |
| `primaryFixed` | #a9bbcd | Slider gradient end, pill dot |
| `secondaryFixed` | #8a9bae | Slider thumb |

### Opacity hierarchy — TEXT
| Level | Alpha | Usage |
|---|---|---|
| Primary | 0.82 | Active sound names, selected values, main content |
| Secondary | 0.55 | Inactive labels, supporting text |
| Tertiary | 0.35–0.40 | Section headers, screen titles |
| Quaternary | 0.25–0.28 | Units, hints, dim labels |
| Ghost | 0.00–0.18 | Invisible / barely present |

### Opacity hierarchy — SURFACES
| Level | Alpha | Usage |
|---|---|---|
| Active card | 0.92 | Active sound tile, custom card |
| Inactive card | 0.72 | Inactive sound tile |
| Subtle card | 0.50–0.60 | Protect Focus row, secondary surfaces |
| Bottom panel | 0.97 | Floating bottom bar |
| Toggle container | 0.03 | Mode toggle outer container |
| Toggle active tab | 0.85 | Mode toggle active pill |

### Forbidden
`Color.White`, `Color.Black`, hardcoded hex in feature code, saturated accents (blue/green/red/orange), `outlineVariant` > 0.18 alpha.

---

## 3. Typography

**Weight philosophy:** Light(300) dominates body/values/names. Normal(400) for labels/headers. Medium(500) **only** for `titleLarge` (card titles). SemiBold(600) **only** on Hero Session Button label. **Bold(700) does not exist.** Max 2 weights per screen.

### Type scale
| Token | Size | Weight | Letter spacing | Usage |
|---|---|---|---|---|
| `displayLarge` | 56sp | Light | -0.03em | Scene name |
| `displayMedium` | 64sp | Light | -0.03em | Timer countdown (tabular) |
| `headlineSmall` | 24sp | Normal | -0.01em | Section headers |
| `titleLarge` | 22sp | Medium | 0 | Card titles only |
| `bodyMedium` | 15sp | Light | -0.01em | Sound names, content |
| `bodySmall` | 13sp | Light | 0.01em | Subtitles |
| `labelMedium` | 11sp | Normal | 0.14em | Phase labels |
| `labelSmall` | 10sp | Normal | 0.12em | Section headers, units, badges |
| `labelTiny` | 9sp | Normal | 0.14em | Hero button label (SemiBold exception) |

### Specific specs
- **Screen headers** ("FOCUS SESSION"): 11sp Normal 0.18em onSurface@0.35 UPPER
- **Card section labels** ("FOCUS","BREAK"): 10sp Normal 0.12em onSurfaceVariant@0.35 UPPER
- **Stepper values** ("25"): 22sp Light -0.02em onSurface@0.82 tabular
- **Timer countdown** ("24:58"): 64sp Light -0.03em onSurface@0.88 tabular
- **Pill / toggle tabs**: 12sp Normal 0.06em UPPER
- **Bottom panel mix summary**: 14sp Light -0.01em onSurface@0.75

---

## 4. Spacing

```kotlin
object Spacing { xs=4 sm=8 md=12 lg=16 xl=24 xxl=32 } // dp
```

### Screen
- Horizontal padding: 24dp
- Top padding (hero): 120dp
- LazyColumn bottom padding: 140dp (clears floating panel)

### Component
- Card: 14dp horizontal, 13dp top, 12dp bottom, 7dp bottom margin
- Card corners: 16dp standard / 14dp subtle
- Section header: 14dp top / 8dp bottom
- Pill row: 12dp vertical, 6dp gap, 30dp height, 15dp corner (full pill), 14dp horizontal padding
- Stepper row: 13dp vertical / 14dp horizontal, divider 0.5dp

---

## 5. Elevation & Depth

Depth is implied by **surface tokens**, not shadows or gradients.

| Layer | Element | Token | Shadow |
|---|---|---|---|
| 0 Base | Screen background | `surface` | none |
| 1 Content | Inactive sound tiles | `surfaceContainer` | none |
| 2 Active | Active sound tiles, cards | `surfaceContainerHigh` | none |
| 3 Floating | Bottom panel, modals | `surfaceContainerHighest` | 8dp |
| 4 Overlay | Sheets, dialogs | `surfaceBright` | 16dp |

Rule: every interactive surface is exactly one step above its container.

---

## 6. Component Library (specs)

### 6.1 ImmersiveBackground (Mixer only)
Forest photo + verticalGradient overlay (Transparent → surface@0.60 @35% → surface@0.92 @55% → surface@1.0). Pill-row scrim 32dp tall (Transparent → surface@0.85 → surface@1.0).

### 6.2 Hero PlayButton (Mixer center)
- Outer breathing ring 124dp, 0.5dp outlineVariant@0.05; scale 1.0↔1.03, alpha 0.4↔0.8, infiniteRepeatable tween(3500) Reverse
- Button circle 108dp, surfaceBright@0.04, 0.5dp outlineVariant@0.13
- Play icon 16dp, onSurface@0.55
- Label "SESSION" 9sp **SemiBold** 0.14em onSurface@0.45 UPPER
- Playing-state radial glow primary@0.07→Transparent, scale 1.0↔1.08, 4s infinite

### 6.3 SoundTile
Active bg surfaceContainerHigh@0.92 + 0.5dp outlineVariant@0.15; inactive surfaceContainer@0.72 + outlineVariant@0.09. Corner 16dp; pad 13t/14h/12b. Icon container 34dp 10dp corners (active surfaceContainerHighest@0.85 outlineVariant@0.16; inactive @0.55/@0.10). Name 15sp Light onSurface@0.82(active)/0.45(inactive). Organic motion icon 18dp tint=primary, alpha {ON+active=0.80, OFF+active=0.28, inactive=0}, animateFloatAsState tween(300). Switch: checkedThumb onSurface@0.92 / track primaryContainer; uncheckedThumb onSurface@0.35 / track surfaceContainerHighest; scale 0.85. Slider track 4dp, active gradient primary→primaryFixed, inactive surfaceContainerHighest, thumb 14dp secondaryFixed.

### 6.4 CategoryPillRow
LazyRow contentPadding 12dp horizontal, spacedBy 6dp, vertical pad 12dp. Active: 30dp h, 15dp corner, surfaceBright@0.55, 0.5dp outlineVariant@0.25, text onSurface@0.88, 4dp primaryFixed dot. Inactive: transparent, 0.5dp outlineVariant@0.18, text onSurfaceVariant@0.58, no dot. 12sp Normal 0.01em. animateColorAsState tween(300). No ripple.

### 6.5 SectionHeader
Row spacedBetween. Left: 10sp Normal 0.12em onSurfaceVariant@0.48 UPPER. Right badge: surfaceContainerHighest@0.70, 10dp corner, 2dp/8dp padding, 10sp onSurfaceVariant@0.45. Row pad: 14t/8b/2h.

### 6.6 SessionModeToggle (Focus/Sleep)
Outer: onSurface@0.03, 0.5dp outlineVariant@0.07, 12dp corner, 3dp pad. Active tab: 34dp h, 9dp corner, surfaceBright@0.85 (Color(0xFF2A3240)@0.85), 0.5dp primary@0.14, text onSurface@0.78. Inactive: transparent, no border, text onSurface@0.25. 12sp Normal 0.06em UPPER. animateColorAsState tween(250). No ripple.

### 6.7 StepperRow (must be pixel-identical on Focus & Sleep screens)
Row: Label (10sp Normal 0.12em onSurfaceVariant@0.35 UPPER) — weight(1) — [-] — 16dp — Value+unit — 16dp — [+]. Stepper button 26dp circle, onSurface@0.04 bg, 0.5dp outlineVariant@0.12, icon onSurface@0.40 17sp Light. Value 22sp Light -0.02em onSurface@0.82 tabular; unit 10sp Normal 0.08em onSurfaceVariant@0.30 UPPER, 3dp gap. Inter-row divider 0.5dp outlineVariant@0.06 full-width.

### 6.8 PresetRow (Focus screen)
Pad 11dp v / 2dp h. Radio 17dp Circle: inactive 0.5dp outlineVariant@0.20; active 0.5dp primary@0.45 + 7dp inner dot primary@0.80. Label 14sp Light -0.01em, inactive onSurface@0.48 / active onSurface@0.80. Use `·` (middle dot) not `/`. tween(250). No ripple.

### 6.9 ProtectFocusCard (subtle card)
surfaceContainer@0.60 bg, 0.5dp outlineVariant@0.08, 14dp corner, 13v/14h pad. Icon container 30dp 8dp corners, primary@0.05 bg, 0.5dp primary@0.10. Shield 15dp stroke primary@0.50 1dp round caps. Title 13sp Light onSurface@0.62. Subtitle 11sp Light onSurface@0.25. Chevron `›` 14sp onSurface@0.15. No ripple — scale(0.98) on press.

### 6.10 StartSessionButton (primary CTA — NEVER white/light-filled)
52dp h, 26dp corner (full pill). Bg primary@0.10, 0.5dp primary@0.20. Label "START SESSION" 11sp Normal 0.16em primary@0.78 UPPER. Press scale(0.98) tween(150). No ripple.

### 6.11 DoneButton / EndButton (secondary)
44dp h, 120dp minWidth, 22dp corner. Bg surfaceContainerHighest@0.80, 0.5dp outlineVariant@0.14. Label 12sp Light 0.08em onSurface@0.55. No ripple.

### 6.12 Session Skip+End controls (Active Session, Focus)
Outer pill 56dp h, 28dp corner, surfaceContainerHighest@0.80, 0.5dp outlineVariant@0.12, 6dp internal pad. Each button transparent, Column(icon, label). Icon 20dp onSurface@0.55. Label 10sp onSurface@0.35. No ripple.

### 6.13 CloseButton
28dp Circle, transparent, 0.5dp outlineVariant@0.14. Icon 14dp onSurface@0.38. Position: full screens top-LEFT padding(start=20, top=16); sheets/modals top-RIGHT padding(end=20, top=16). scale(0.95) on press, no ripple.

### 6.14 CurrentMixPanel (floating bottom)
Bg surfaceContainerHighest verticalGradient 97%→93%. Top border 0.5dp outlineVariant@0.09. 18dp corner (top only). 8dp shadow. Pad 11t/16h/26b (safe area). "CURRENT MIX" 10sp Normal 0.10em onSurfaceVariant@0.40 UPPER. Mix text 14sp Light onSurface@0.75. Action btn 32dp circle, onSurface@0.06 bg, 0.5dp outlineVariant@0.10, icon onSurface@0.60 14dp. Chevron 22dp onSurface@0.28. No ripple.

### 6.15 AtmosphericField (Active Session — breathing circle w/ timer)
Outer glow ring ~300dp Circle, radialGradient(primary@0.06 → Transparent); scale 1.0↔1.06, alpha 0.6↔1.0, infiniteRepeatable tween(4000) Reverse. Inner circle ~200dp Circle, radialGradient(surfaceContainerHigh@0.50 → Transparent), 0.5dp outlineVariant@0.12. Timer 64sp Light tabular onSurface@0.88. Pause button (Focus only) 52dp Circle, surfaceContainerHigh@0.80, 0.5dp outlineVariant@0.15, icon onSurface@0.65 20dp. No ripple.

---

## 7. Animation System

### Easing — `core/designsystem/theme/Motion.kt`
```kotlin
object FocusRitualEasing {
  val DeepEaseOut   = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f) // premium settle
  val CinematicIn   = CubicBezierEasing(0.55f, 0.0f, 0.85f, 0.0f) // exits
  val Atmospheric   = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f) // slow heavy
  val Ritual        = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f) // snap+hang
}
```

### Durations
| Speed | ms | Usage |
|---|---|---|
| Instant | 150 | Press feedback |
| Quick | 200–250 | Toggle, icon swap |
| Medium | 300–320 | Color/alpha transitions |
| Deliberate | 400–480 | Screen panel enter/exit |
| Slow | 550–600 | Session start dive |
| Ceremonial | 700 | Session complete, Mixer re-entry |

**Rule:** Entering screens always longer than exiting screens.

### Specs reference
- Color/alpha state: `animateColorAsState/animateFloatAsState(tween(300))`
- Press: `tween(150)` scale 1.0→0.97→1.0
- Hero breathing: `infiniteRepeatable(tween(3500), Reverse)`
- Organic volume drift: `spring(dampingRatio=0.75f, stiffness=200f)`
- Glow breathing: `infiniteRepeatable(tween(4000), Reverse)`
- Panel enter/exit: `fadeIn+slideInVertically tween(400)` / `fadeOut+slideOutVertically tween(300)`

### Screen transitions
| Transition | Enter | Exit |
|---|---|---|
| Mixer → FocusSession | 480ms DeepEaseOut | 360ms Atmospheric |
| FocusSession → Mixer | 500ms Atmospheric | 340ms CinematicIn |
| FocusSession → ActiveSession | 600ms Ritual + 80ms delay | 300ms CinematicIn |
| ActiveSession → Mixer | 700ms Atmospheric | 200ms Linear |
| Fallback | 300ms Atmospheric | 260ms CinematicIn |

---

## 8. Motion Rules
1. No bounce — nothing springs past target and back.
2. No linear easing — always `FocusRitualEasing.*`.
3. Min fade 260ms (shorter feels like a cut).
4. Max scale range 0.95–1.04 (beyond = zoom).
5. `delayMillis` only on entering `fadeIn`, never on scale.
6. Max delay 100ms.
7. Entering > exiting.
8. Never stack animations back-to-back without content between them.

---

## 9. Interaction Rules
1. No ripple anywhere — `indication = null` on every clickable.
2. Press: scale(0.97) tween(150), restore on release.
3. No system `Button`/`FilledButton` — always custom `Box + clickable`.
4. No long-press, no swipe, no double-tap. Single tap only.

---

## 10. Border Rules
1. Width: **0.5dp everywhere** — never 1dp.
2. Color: `outlineVariant` or `primary` at low opacity only.
3. Max opacity: 0.25 (primary) / 0.18 (outlineVariant).
4. No borders on text or icons.
5. Rounded corners only on elements with borders on **all** sides.

---

## 11. Hard NEVERS
White/light-filled buttons · Colored accent buttons · Ripples · Bold(700) · Pure white text · Divider lines between list items · Hard shadows on cards · Decorative gradients · Gamification (streaks/points/badges/confetti) · Stats/metrics on completion · Bright/saturated colors · Bouncy springs · Icon+text on navigation · >2 font weights per screen · Borders >0.5dp · `outlineVariant` >0.18 alpha.

---

## 12. Screen-by-Screen

- **Mixer (Home):** forest photo + gradient; 108dp glassmorphic hero w/ breathing rings; LazyColumn pills/headers/tiles; floating panel `surfaceContainerHighest`. Register: ambient/alive/exploratory.
- **Focus Session Setup:** pure `surface` background; screen header; mode toggle; preset rows + custom card + ProtectFocus row; primary CTA. Register: deliberate/calm.
- **Sleep Session Setup:** pure `surface`; identical spec, Sleep tab active, different card content. Register: softer/winding-down.
- **Active Session — Focus:** forest photo + darker overlay + phase-aware shift; AtmosphericField w/ timer + pause; cycle dots + Skip+End pill. Register: immersive/protected/ritual.
- **Active Session — Sleep:** forest photo + darkest overlay; AtmosphericField w/ timer (no pause); single End pill; fade-to-black overlay during fade-out. Register: drifting/dissolving.
- **Session Complete:** pure `surface`, breathing glow only; expanding rings + checkmark; "Well done." text only; two CTAs (return / start another). Register: stillness/quiet. (No stats.)
- **Current Mix Modal:** `surfaceContainerLowest` overlay; organic motion toggle + active sound cards; Done = secondary action. Register: fine-tuning/intimate.

---

## 13. Code Architecture Rules

Feature code (`feature/*`) **may only** use:
- `MaterialTheme.colorScheme.*`
- `MaterialTheme.typography.*`
- `Spacing.*`
- `FocusRitualEasing.*`

**Forbidden in feature code:**
- `import core.designsystem.theme.Color.*`
- `Color(0xFF...)` — exception: `Color(0xFF2A3240)` for surfaceBright in toggle
- `Color.White`, `Color.Black`

Hardcoded hex is allowed **only** in `core/designsystem/*`.

---

## 14. File Map

| File | Responsibility |
|---|---|
| `core/designsystem/theme/Color.kt` | All color constants |
| `core/designsystem/theme/Type.kt` | `FocusRitualTypography` |
| `core/designsystem/theme/Theme.kt` | `FocusRitualTheme`, `darkColorScheme` wiring |
| `core/designsystem/theme/Spacing.kt` | `Spacing` object |
| `core/designsystem/theme/Motion.kt` | `FocusRitualEasing` |
| `core/designsystem/component/SoundTile.kt` | Sound card + icon |
| `core/designsystem/component/VolumeSlider.kt` | Slider w/ organic motion |
| `core/designsystem/component/PlayButton.kt` | Hero w/ rings |
| `core/designsystem/component/CategoryPillRow.kt` | Filter pills |
| `core/designsystem/component/SessionModeToggle.kt` | Focus/Sleep toggle |
| `core/designsystem/component/StepperRow.kt` | Stepper control row |
| `core/designsystem/component/CloseButton.kt` | × button |
| `core/designsystem/component/StartSessionButton.kt` | Primary CTA |
| `feature/mixer/MixerScreen.kt` | Home |
| `feature/session/FocusSessionScreen.kt` | Session config |
| `feature/timer/ActiveSessionScreen.kt` | Running timer |
| `feature/timer/SessionCompleteScreen.kt` | Completion moment |
