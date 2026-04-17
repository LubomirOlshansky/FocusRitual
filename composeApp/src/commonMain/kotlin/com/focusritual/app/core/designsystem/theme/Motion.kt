package com.focusritual.app.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing

object FocusRitualEasing {
    // Deep ease-out — starts fast, settles slowly into place. Premium feel.
    val DeepEaseOut = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)

    // Cinematic ease-in — starts slow, accelerates with intention. Used for exits.
    val CinematicIn = CubicBezierEasing(0.55f, 0.0f, 0.85f, 0.0f)

    // Atmospheric — extremely slow ease both sides. Used for the Mixer re-entry.
    val Atmospheric = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

    // Ritual — asymmetric. Snaps in gently, hangs in the air. Session start only.
    val Ritual = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
}
