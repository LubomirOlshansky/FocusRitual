package com.focusritual.app.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val mixWithOthersEnabled: Boolean = false,
    val duckOthersEnabled: Boolean = false,
    val mixWithOthersVolume: Float = 0.5f,
    val duckLevel: Float = 0.30f,
    val hapticsEnabled: Boolean = true,
    val activeDetail: SettingsDetail? = null,
    val onboardingCompleted: Boolean = true,
)

enum class SettingsDetail {
    SoundCredits,
    PrivacyPolicy,
    TermsOfUse,
}

sealed interface SettingsIntent {
    data class SetMixWithOthers(val enabled: Boolean) : SettingsIntent
    data class SetDuckOthers(val enabled: Boolean) : SettingsIntent
    data class SetMixWithOthersVolume(val volume: Float) : SettingsIntent
    data class SetDuckLevel(val level: Float) : SettingsIntent
    data class SetHapticsEnabled(val enabled: Boolean) : SettingsIntent
    data object OpenLanguageSettings : SettingsIntent
    data object RateApp : SettingsIntent
    data object ShareApp : SettingsIntent
    data object ContactSupport : SettingsIntent
    data object OpenSoundCredits : SettingsIntent
    data object OpenPrivacyPolicy : SettingsIntent
    data object OpenTermsOfUse : SettingsIntent
    data object CloseDetail : SettingsIntent
    data object ResetToHome : SettingsIntent
    data class SetOnboardingCompleted(val completed: Boolean) : SettingsIntent
}

sealed interface SettingsEffect {
    data object OpenLanguageSettings : SettingsEffect
    data object RateApp : SettingsEffect
    data object ShareApp : SettingsEffect
    data class ContactSupport(val email: SettingsSupportEmail = SettingsSupportEmail()) : SettingsEffect
}

data class SettingsSupportEmail(
    val to: String = "hello@focusritual.app",
    val subject: String = "FocusRitual - Feedback",
) {
    fun body(appVersion: String): String = "Version: $appVersion\n\n"
}