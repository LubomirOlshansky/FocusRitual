package com.focusritual.app.core.platformaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

interface PlatformActions {
    fun openLanguageSettings()
    fun rateApp()
    fun shareApp()
    fun sendEmail(to: String, subject: String, body: String)
    fun openUrl(url: String)
    val appVersion: String
    val currentLanguageName: String
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> {
    error("PlatformActions is not provided")
}

@Composable
expect fun rememberPlatformActions(): PlatformActions

@Composable
fun ProvidePlatformActions(content: @Composable () -> Unit) {
    val platformActions = rememberPlatformActions()
    CompositionLocalProvider(LocalPlatformActions provides platformActions) {
        content()
    }
}