package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun LuxuryCardSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(Color(0xFF191E25))
            .border(
                width = 0.5.dp,
                color = Color(0xFFFFFFFF).copy(alpha = 0.07f),
                shape = cardShape,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFFFFF).copy(alpha = 0.10f),
                            Color(0xFFFFFFFF).copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        content()
    }
}