package com.focusritual.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun ProtectFocusCard(
    isConfigured: Boolean,
    blockedAppCount: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEditBlockedApps: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val contentAlpha = if (isConfigured && !isEnabled) 0.45f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = shape)
            .then(
                if (!isConfigured) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onClick() }
                } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        if (!isConfigured) {
            NotConfiguredContent(onClick, contentAlpha)
        } else {
            ConfiguredContent(
                blockedAppCount = blockedAppCount,
                isEnabled = isEnabled,
                onToggle = onToggle,
                onEditBlockedApps = onEditBlockedApps,
                contentAlpha = contentAlpha,
            )
        }
    }
}

@Composable
private fun NotConfiguredContent(onClick: () -> Unit, contentAlpha: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f * contentAlpha),
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = "Protect Focus",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f * contentAlpha),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Keep distracting apps outside this session",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f * contentAlpha),
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
        )
    }
}

@Composable
private fun ConfiguredContent(
    blockedAppCount: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEditBlockedApps: () -> Unit,
    contentAlpha: Float,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f * contentAlpha),
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Protect Focus",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f * contentAlpha),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$blockedAppCount apps selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f * contentAlpha),
                )
            }

            Spacer(Modifier.width(12.dp))

            ProtectFocusToggle(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Edit blocked apps",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f * contentAlpha),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onEditBlockedApps() }
                .padding(start = 32.dp),
        )
    }
}

@Composable
private fun ProtectFocusToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    width: Dp = 44.dp,
    height: Dp = 26.dp,
    thumbSize: Dp = 20.dp,
) {
    val trackColor = animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(300),
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(CircleShape)
            .background(trackColor.value)
            .border(
                width = 1.dp,
                color = if (checked) Color.Transparent else Color.White.copy(alpha = 0.08f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (checked) 0.95f else 0.70f)),
        )
    }
}
