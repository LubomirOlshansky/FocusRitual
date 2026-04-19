package com.focusritual.app.feature.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.int

class SessionPreferences(private val settings: Settings = Settings()) {
    var customFocusMinutes by settings.int("session.custom.focus_min", 25)
    var customBreakMinutes by settings.int("session.custom.break_min", 5)
    var customSessions by settings.int("session.custom.sessions", 4)
    var sleepDurationMinutes by settings.int("session.sleep.duration_min", 45)
    var sleepFadeOutMinutes by settings.int("session.sleep.fade_out_min", 10)
}
