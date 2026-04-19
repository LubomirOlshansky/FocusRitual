package com.focusritual.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun StepperRow(
    label: String,
    value: Int,
    unit: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.01).em,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StepperButton(icon = Icons.Default.Remove, onClick = onDecrement)
            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.02).em,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
                Text(
                    text = unit.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.08.em,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 14.dp, y = 4.dp),
                )
            }
            StepperButton(icon = Icons.Default.Add, onClick = onIncrement)
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
            modifier = Modifier.size(14.dp),
        )
    }
}
