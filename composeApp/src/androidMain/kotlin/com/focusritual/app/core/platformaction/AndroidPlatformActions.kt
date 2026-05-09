package com.focusritual.app.core.platformaction

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.focusritual.app.BuildConfig

class AndroidPlatformActions(
    private val context: Context,
) : PlatformActions {
    override fun openLanguageSettings() {
        val packageUri = Uri.parse("package:${context.packageName}")
        val openedLocaleSettings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, packageUri))
        } else {
            false
        }

        if (!openedLocaleSettings) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        }
    }

    override fun rateApp() {
        val packageName = context.packageName
        val openedMarket = startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
        )
        if (!openedMarket) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                ),
            )
        }
    }

    override fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, ShareText)
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    override fun sendEmail(to: String, subject: String, body: String) {
        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        if (startActivity(mailIntent)) return

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        if (startActivity(Intent.createChooser(sendIntent, null))) return

        copyToClipboard(label = "FocusRitual email", text = to)
    }

    override fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override val appVersion: String
        get() = runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: BuildConfig.VERSION_NAME
        }.getOrElse { BuildConfig.VERSION_NAME }

    override val currentLanguageName: String
        get() = runCatching {
            val locale = context.resources.configuration.locales[0]
            locale.displayLanguage
        }.getOrElse { "English" }

    private fun startActivity(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

private const val ShareText = "FocusRitual - ambient focus sanctuary\nhttps://focusritual.app"