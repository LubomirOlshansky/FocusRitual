package com.focusritual.app.core.protectfocus

interface ScreenTimeHandler {
    fun requestSetup(
        onSuccess: () -> Unit,
        onCancelled: () -> Unit,
        onDenied: () -> Unit,
    )
}

object ScreenTimeBridge {
    var useMock: Boolean = true
    var handler: ScreenTimeHandler? = if (useMock) MockScreenTimeHandler() else null
}
