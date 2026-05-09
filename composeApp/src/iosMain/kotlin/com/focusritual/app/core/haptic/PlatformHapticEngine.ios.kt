package com.focusritual.app.core.haptic

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType


actual class PlatformHapticEngine actual constructor() : HapticEngine {
    actual override fun perform(type: HapticFeedbackType) {
        when (type) {
            HapticFeedbackType.LightImpact -> UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).apply {
                prepare()
                impactOccurred()
            }
            HapticFeedbackType.MediumImpact -> UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).apply {
                prepare()
                impactOccurred()
            }
            HapticFeedbackType.Success -> UINotificationFeedbackGenerator().apply {
                prepare()
                notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
        }
    }
}
