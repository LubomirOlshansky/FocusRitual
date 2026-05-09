package com.focusritual.app.core.haptic

interface HapticEngine {
    fun perform(type: HapticFeedbackType)
}

expect class PlatformHapticEngine() : HapticEngine {
    override fun perform(type: HapticFeedbackType)
}
