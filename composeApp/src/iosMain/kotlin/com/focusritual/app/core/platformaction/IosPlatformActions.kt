package com.focusritual.app.core.platformaction

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
class IosPlatformActions : PlatformActions {
    override fun openLanguageSettings() {
        if (!open(UIApplicationOpenSettingsURLString)) {
            open("app-settings:")
        }
    }

    override fun rateApp() {
        val openedStore = open("itms-apps://itunes.apple.com/app/id$FocusRitualAppStoreId?action=write-review")
        if (!openedStore) {
            open("https://apps.apple.com/app/id$FocusRitualAppStoreId?action=write-review")
        }
    }

    override fun shareApp() {
        val topController = topViewController()
        if (topController == null) {
            copyToPasteboard(ShareText)
            return
        }

        val activityController = UIActivityViewController(
            activityItems = listOf(ShareText),
            applicationActivities = null,
        )
        topController.presentViewController(activityController, animated = true, completion = null)
    }

    override fun sendEmail(to: String, subject: String, body: String) {
        val mailto = "mailto:$to?subject=${urlEncode(subject)}&body=${urlEncode(body)}"
        if (open(mailto)) return

        val topController = topViewController()
        if (topController != null) {
            val shareText = "Contact: $to\nSubject: $subject\n\n$body"
            val activityController = UIActivityViewController(
                activityItems = listOf(shareText),
                applicationActivities = null,
            )
            topController.presentViewController(activityController, animated = true, completion = null)
        } else {
            copyToPasteboard(to)
        }
    }

    override fun openUrl(url: String) {
        open(url)
    }

    override val appVersion: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0"

    override val currentLanguageName: String
        get() {
            val languageCode = (NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String)
                ?.substringBefore("-")
                ?.substringBefore("_")
                ?: "en"
            
            return when (languageCode.lowercase()) {
                "en" -> "English"
                "es" -> "Español"
                "fr" -> "Français"
                "de" -> "Deutsch"
                "pt" -> "Português"
                "zh" -> "简体中文"
                "ja" -> "日本語"
                "ko" -> "한국어"
                "hi" -> "हिन्दी"
                "ar" -> "العربية"
                else -> "English"
            }
        }

    private fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val application = UIApplication.sharedApplication
        if (!application.canOpenURL(nsUrl)) return false
        application.openURL(nsUrl)
        return true
    }

    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }

    private fun copyToPasteboard(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}

private const val FocusRitualAppStoreId = "0000000000"
private const val ShareText = "FocusRitual - ambient focus sanctuary\nhttps://focusritual.app"

private fun urlEncode(value: String): String = value.encodeToByteArray().joinToString(separator = "") { byte ->
    val intValue = byte.toInt() and 0xff
    val char = intValue.toChar()
    if (char.isLetterOrDigit() || char in "-_.~") {
        char.toString()
    } else {
        "%" + intValue.toString(16).uppercase().padStart(2, '0')
    }
}