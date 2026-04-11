package com.focusritual.app.core.protectfocus

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ProtectFocusController actual constructor() {
    actual suspend fun requestSetup(): SetupResult = suspendCancellableCoroutine { cont ->
        val handler = ScreenTimeBridge.handler
        if (handler == null) {
            cont.resume(SetupResult.Cancelled)
            return@suspendCancellableCoroutine
        }
        handler.requestSetup(
            onSuccess = { if (cont.isActive) cont.resume(SetupResult.Success) },
            onCancelled = { if (cont.isActive) cont.resume(SetupResult.Cancelled) },
            onDenied = { if (cont.isActive) cont.resume(SetupResult.PermissionDenied) },
        )
    }
}
