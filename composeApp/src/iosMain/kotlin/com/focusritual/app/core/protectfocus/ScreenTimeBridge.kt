package com.focusritual.app.core.protectfocus

interface ScreenTimeHandler {
    fun requestSetup(
        onSuccess: () -> Unit,
        onCancelled: () -> Unit,
        onDenied: () -> Unit,
    )
}

object ScreenTimeBridge {
    var handler: ScreenTimeHandler? = null
}
