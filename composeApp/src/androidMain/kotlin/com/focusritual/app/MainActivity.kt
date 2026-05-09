package com.focusritual.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.focusritual.app.core.audio.AndroidAudioContext
import com.focusritual.app.core.haptic.AndroidHapticContext
import com.focusritual.app.core.platformaction.ProvidePlatformActions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidAudioContext.init(this)
        AndroidHapticContext.init(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ProvidePlatformActions {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    ProvidePlatformActions {
        App()
    }
}