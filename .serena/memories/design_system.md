# FocusRitual — Design System (Cinematic Ambience)

## Creative Direction: "The Digital Sanctuary"
A high-end, dimly lit listening room. Intentional asymmetry, tonal depth. 
Screen = canvas of light and shadow, not a collection of boxes.

## Color Tokens (Dark theme only)

### Surfaces
| Token | Hex | Usage |
|-------|-----|-------|
| surface | #0c0e11 | Base layer, screen background |
| surface_container_lowest | #000000 | Recessed elements |
| surface_container_low | #111418 | Secondary sections |
| surface_container | #161a1f | Content areas |
| surface_container_high | ~#1b2027 | Card backgrounds |
| surface_container_highest | #20262e | Floating elements |
| surface_bright | ~#2a3240 | Critical interactive elements |

### Primary
| Token | Hex | Usage |
|-------|-----|-------|
| primary | #b7c8db | Main CTA, active states |
| primary_container | #384858 | Active card backgrounds |
| primary_dim | #a9bbcd | Glow effects, gradient endpoints |

### Content
| Token | Hex | Usage |
|-------|-----|-------|
| on_surface | #e0e6f1 | Primary text (warm off-white, NEVER #ffffff) |
| on_surface_variant | #a5abb6 | Secondary text, subtitles |
| outline_variant | #424851 | Ghost borders at 15% opacity only |

### Accent
| Token | Hex | Usage |
|-------|-----|-------|
| tertiary | #fff8f4 | Warmest white allowed |
| tertiary_fixed_dim | #efe0d0 | Ambient pulse (10% opacity) |
| secondary_fixed_dim | ~#8a9bae | Slider thumb |

## Core Rules
1. **No-Line Rule:** NO 1px borders. Use tonal surface shifts only.
2. **Glass Effect:** surface_bright at 60% opacity + layered alpha (no native backdrop-blur in Compose)
3. **Signature Gradient:** primary → primary_container at 135° for active CTAs
4. **No Pure White:** Use on_surface (#e0e6f1) or tertiary (#fff8f4), never #ffffff
5. **300ms Transitions:** All state changes use 300ms cubic-bezier
6. **Ambient Shadows:** 0px 24px 48px rgba(0,0,0,0.4) for floating elements
7. **Ghost Borders:** outline_variant at 15% opacity only, never 100%

## Typography
- Font: System default (Manrope planned, add via composeResources/font/ later)
- display-lg → displayLarge: 56sp (scene name, wide letter spacing)
- headline-sm → headlineSmall: 24sp (section headers)
- title-lg → titleLarge: 22sp (card titles)
- body-md → bodyMedium: 14sp (general text)
- label-sm → labelSmall: 11sp + 0.05em letter spacing (subtitles, technical labels)

## Component Patterns
- **PlayButton:** ~96dp circle, glass background, play/pause Material icon, ambient shadow
- **Sound Tiles:** Semi-transparent surfaceContainerHigh 80% opacity, xl corners (24dp), toggle + volume
- **ImmersiveBackground:** Dark forest image (`Res.drawable.background`) + vertical gradient overlay (transparent → 0.6 → 0.92 → opaque)
- **Breathing Circle:** 280dp main circle + 320dp outer ring, scale animation (0.97↔1.03), phase-aware speed (4s focus / 6s break)
- **Session Timer:** 64sp ExtraLight time display, Crossfade digit transitions, centered in breathing circle
- **Progress Dots:** Horizontal row of 32×2dp bars, animated fill by cycle progress
- **Bottom Floating Pill:** RoundedCornerShape(999dp), surfaceBright 60%, skip + stop controls
- **Ghost Borders:** outline_variant at low alpha only, never solid
- **Lists:** NO divider lines, separate with 16dp spacing
- **Spacing:** Use generous void — 112dp+ page margins for cinematic scale