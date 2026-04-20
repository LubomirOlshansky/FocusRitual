package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
internal fun SaveMixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Save this mix",
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}


@Composable
internal fun DoneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
        )
        Text(
            text = "Done",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.04.em,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
        )
    }
}

@Composable
private fun ModalActionButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "modalActionButtonScale",
    )
    val buttonShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(48.dp)
            .clip(buttonShape)
            .background(backgroundColor)
            .border(width = 0.5.dp, color = borderColor, shape = buttonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
