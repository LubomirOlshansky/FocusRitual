package com.focusritual.app.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class PlatformHapticEngine actual constructor() : HapticEngine {
    actual override fun perform(type: HapticFeedbackType) {
        val vibrator = resolveVibrator() ?: return
        if (!vibrator.hasVibrator()) return

        vibrator.vibrate(effectFor(type))
    }

    private fun resolveVibrator(): Vibrator? {
        val context = AndroidHapticContext.appContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun effectFor(type: HapticFeedbackType): VibrationEffect = when (type) {
        HapticFeedbackType.LightImpact -> VibrationEffect.createOneShot(18L, 64)
        HapticFeedbackType.MediumImpact -> VibrationEffect.createOneShot(28L, 128)
        HapticFeedbackType.Success -> VibrationEffect.createWaveform(
            longArrayOf(0L, 24L, 42L, 32L),
            intArrayOf(0, 72, 0, 128),
            -1,
        )
    }
}
