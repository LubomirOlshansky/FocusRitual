package com.focusritual.app.feature.onboarding.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun StepIndicator(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
        fontSize = 8.5.sp,
        letterSpacing = 0.3.em,
        fontWeight = FontWeight.W300,
    )
}
