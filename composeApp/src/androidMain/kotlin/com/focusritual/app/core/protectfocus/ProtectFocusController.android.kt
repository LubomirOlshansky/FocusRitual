package com.focusritual.app.core.protectfocus

actual class ProtectFocusController actual constructor() {
    actual suspend fun requestSetup(): SetupResult = SetupResult.Cancelled
}
