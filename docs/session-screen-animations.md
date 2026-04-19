# Session Screen Animations

How every animated layer works during a Focus or Sleep session, and how it transitions to the completion screen.

---

## Layer stack

The session screen is a `Box` composable with layers painted back-to-front:

```
1. TimerBackground       — forest photo + darkening overlay
2. AmbientBackgroundPulse — full-screen radial colour bloom
3. Sleep fade-to-black overlay (conditional)
4. AnimatedContent       — timer UI  ↔  completion screen
```

---

## Layer 1 — `TimerBackground`

A forest photo with a vertical dark overlay on top (`#0c0e11`).

| State | Overlay alpha |
|---|---|
| Focus phase | 0.82 |
| Break phase | 0.92 |
| Paused | 0.90 |
| Sleep fade-out (progress `t`) | `base + (1 − base) × t` → approaches 1.0 fully opaque |

The alpha change transitions over **1500 ms** with a simple `tween`. During sleep fade-out a `darkenOverride` value driven by the sleep fade float is passed in, smoothly pushing the overlay toward solid black independently of phase.

---

## Layer 2 — `AmbientBackgroundPulse`

A full-screen `Box` with a radial gradient (midnight-blue / blue-grey, centre → transparent). It pulses via a single `infiniteRepeatable` float that drives its `scaleX/Y`:

```
scale = 0.9 + pulse × 0.2   →   range 0.90 → 1.10
```

| State | Half-cycle (one direction) |
|---|---|
| Focus | 7 000 ms |
| Break | 11 000 ms |
| Paused | 14 000 ms |

The bloom's maximum opacity (`targetAlpha`) also transitions over 1500 ms when phase changes:

| State | Max alpha |
|---|---|
| Focus | 0.060 |
| Break | 0.025 |
| Paused | 0.015 |

---

## Layer 3 — Sleep fade-to-black overlay (sleep mode only)

An additional full-screen `Box` with `Color.Black.copy(alpha = (1 − sleepFade) × 0.6)`. Invisible at the start of the fade-out (`sleepFade = 1 → alpha = 0`) and peaks at 60 % opacity as `sleepFade → 0`. It layers on top of the darkening forest so the overall feel is a slow, heavy fade to near-black.

---

## Layer 4 — `AtmosphericField` (the centre orb)

The main 320 dp glassmorphic circle housing the timer. Four sub-layers inside it:

### Breathing system

Two independent breath oscillators are blended:

| Oscillator | Focus half-cycle | Break | Paused | Sleep |
|---|---|---|---|---|
| Primary (fast) | 4 800 ms | 5 800 ms | 6 000 ms | 6 500 ms |
| Secondary (slow) | 7 200 ms | 9 000 ms | 8 500 ms | 10 000 ms |

`breath = primary × 0.7 + secondary × 0.3`

An `intensity` value smoothly transitions (2000 ms) based on phase:

| State | Intensity |
|---|---|
| Focus | 1.00 |
| Break | 0.50 |
| Paused | 0.30 |
| Sleep | 0.45 |

The combined `b = breath × intensity` drives all layer values below.

### Sub-layer: Outer breathing pulse (480 dp)
- `scaleX/Y = 1.0 + b × 0.10` (smaller in sleep: `× 0.6`)
- `alpha = (0.5 + b × 0.5) × intensity`

### Sub-layer: Counter-shadow ring (370 dp)
Darkens when the breath contracts — adds depth when the orb "inhales".
- `alpha = (0.06 + (1 − b) × 0.10) × intensity`

### Sub-layer: Glow halo (360 dp)
A luminance ring that brightens as the orb "exhales".
- `alpha = (0.15 + b × 0.10) × intensity`

### Sub-layer: Glassmorphic circle (320 dp) + inner light drift
The circle itself doesn't scale, but a soft light mass drawn with `drawBehind` wanders around inside it:

| Drift axis | Focus | Break | Paused | Sleep |
|---|---|---|---|---|
| X half-cycle | 11 000 ms | 15 000 ms | 18 000 ms | 20 000 ms |
| Y half-cycle | 14 000 ms | 19 000 ms | 22 000 ms | 25 000 ms |

Drift visibility (`driftIntensity`) scales with phase over 2500 ms:

| State | Drift intensity |
|---|---|
| Focus | 1.00 |
| Break | 0.40 |
| Paused | 0.15 |
| Sleep | 0.25 |

The light is always slightly off-centre and moves asynchronously on both axes, giving the orb an organic, alive quality even when the user isn't looking.

---

## Audio sync

Volume is driven by `animateFloatAsState(tween(400 ms))` and mirrors the visual state:

| State | Target volume |
|---|---|
| Exiting (stop / start-another) | 0 → ramps down 400 ms then exits |
| Sleep fading out | Tracks `sleepFade` (same duration as the visual fade) |
| Completed | 0 |
| Paused | 0 |
| Focus phase | 1 |
| Break phase | 0 |

---

## Sleep mode completion path

When the sleep timer runs out (or the user taps the exit button):

1. `isSleepFadingOut = true` is set in the ViewModel.
2. `sleepFade` animates `1 → 0` over `sleepFadeOutMinutes × 60 000 ms` (default 30 s).
3. During the fade:
   - `TimerBackground` overlay darkens toward fully opaque.
   - Sleep fade-to-black overlay becomes visible (up to 60 % black).
   - `AtmosphericField` `fadeFraction` = `sleepFade` — orb breathes dimmer.
   - Timer text is hidden; top bar is replaced by a spacer.
   - Audio tracks `sleepFade` down simultaneously.
4. When `sleepFade == 0`, a 500 ms `LaunchedEffect` fires then calls `onFinish()`.

The sleep screen never shows the completion screen — it exits directly to Mixer.

---

## Focus mode completion path

When the last cycle ends (naturally or via Skip):

1. `isCompleted = true` is set. **The last focus cycle never starts a break** — `advanceCycle()` is called directly.
2. `AnimatedContent` keyed on `isCompleted && !isSleepMode` fires its transition:

| Element | Spec |
|---|---|
| Completion screen enters | `fadeIn(600 ms, Atmospheric) + scaleIn(700 ms, Ritual, initialScale = 0.97)` |
| Timer UI exits | `fadeOut(400 ms)` |

3. During the exit fade, the timer text and phase label have `graphicsLayer { alpha = 0 }` applied — so "0:00" and "COMPLETE" are invisible while fading out, producing a clean dark-to-completion reveal.

---

## `SessionCompleteScreen` — internal animation timeline

Once the completion screen is visible, its own staggered entry begins from `t = 0` (screen mount):

| Element | Delay | Duration | Easing |
|---|---|---|---|
| Outer ring (140 dp) | 100 ms | 2 400 ms | Ritual |
| Mid ring (108 dp) | 350 ms | 2 400 ms | Ritual |
| Inner ring (78 dp) | 600 ms | 2 400 ms | Ritual |
| Checkmark circle scale | 500 ms | 700 ms | Ritual |
| Checkmark path fade | 900 ms | 500 ms | Linear |
| Headline "Well done." | 650 ms | 900 ms | Ritual |
| Subline | 800 ms | 900 ms | Ritual |
| Bottom CTAs | 1 100 ms | 900 ms | Ritual |
| Background glow breathe (∞) | — | 5 000 ms/cycle | Atmospheric |
| Particles drift (14×, ∞) | staggered 0–8 s | 10–22 s/cycle | Linear |

**Rings** animate from `scale 0.82 → 1.0` and an alpha curve: `0 → 0.20` (first 25% of tween) then `0.20 → 0.07` (remaining 75%), settling at a barely-visible border.

**Background glow** — two radial gradients (320 dp outer, 180 dp inner) breathe scale `1.0 ↔ 1.08` and alpha `0.6 ↔ 1.0`. The inner glow is offset by 800 ms so the two layers never peak together.

**Particles** — 14 dots, 2 dp each, with fixed seeds so they're stable across recompositions. Each drifts upward 200 dp over 10–22 s, scales from 1.0 → 0.3, and has a fade-in/hold/fade-out alpha envelope.

---

## Easing reference

| Name | Bezier | Used for |
|---|---|---|
| `Ritual` | `(0.22, 1.0, 0.36, 1.0)` | Entry animations — snaps in, hangs in the air |
| `Atmospheric` | `(0.25, 0.46, 0.45, 0.94)` | Slow transitions — mixer re-entry, glow breathe |
| `DeepEaseOut` | `(0.16, 1.0, 0.30, 1.0)` | Fast but premium settle |
| `CinematicIn` | `(0.55, 0.0, 0.85, 0.0)` | Exits — starts slow, accelerates |
