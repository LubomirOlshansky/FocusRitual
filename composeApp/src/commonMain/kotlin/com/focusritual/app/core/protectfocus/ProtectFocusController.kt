package com.focusritual.app.core.protectfocus

expect class ProtectFocusController() {
    suspend fun requestSetup(): SetupResult
}
