package com.focusritual.app.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp

actual fun Modifier.softBlur(radius: Dp): Modifier = this.blur(radius)
