package com.focusritual.app.core.protectfocus

sealed interface ProtectFocusState {
    data object Idle : ProtectFocusState
    data object SheetOpen : ProtectFocusState
    data object SettingUp : ProtectFocusState
    data object SetupCompleted : ProtectFocusState
    data object SetupCancelled : ProtectFocusState
    data object PermissionDenied : ProtectFocusState
}

sealed interface SetupResult {
    data object Success : SetupResult
    data object Cancelled : SetupResult
    data object PermissionDenied : SetupResult
}
