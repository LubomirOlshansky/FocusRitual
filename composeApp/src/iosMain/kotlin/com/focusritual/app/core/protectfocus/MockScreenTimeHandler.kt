package com.focusritual.app.core.protectfocus

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MockScreenTimeHandler : ScreenTimeHandler {
    @OptIn(DelicateCoroutinesApi::class)
    override fun requestSetup(
        onSuccess: () -> Unit,
        onCancelled: () -> Unit,
        onDenied: () -> Unit,
    ) {
        GlobalScope.launch {
            delay(1500L)
            onSuccess()
        }
    }
}
