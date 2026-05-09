package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.mixer_organic_motion
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GlobalOrganicMotionRow(
    isOrganicMotionEnabled: Boolean,
    organicMotionSummary: String,
    allSoundsOrganic: Boolean,
    onToggle: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val summaryColor = when {
        !isOrganicMotionEnabled -> colorScheme.onSurface.copy(alpha = 0.42f)
        allSoundsOrganic -> colorScheme.primary.copy(alpha = 0.70f)
        else -> colorScheme.primary.copy(alpha = 0.60f)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isOrganicMotionEnabled) {
                    colorScheme.primary.copy(alpha = 0.60f)
                } else {
                    colorScheme.onSurface.copy(alpha = 0.42f)
                },
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(Res.string.mixer_organic_motion),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = colorScheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = organicMotionSummary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = summaryColor,
            )

            Spacer(Modifier.weight(1f))

            Switch(
                checked = isOrganicMotionEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onSurface.copy(alpha = 0.92f),
                    checkedTrackColor = colorScheme.primaryContainer,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.28f),
                    uncheckedTrackColor = colorScheme.surfaceContainerHighest,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
                .height(0.5.dp)
                .background(colorScheme.outlineVariant.copy(alpha = 0.08f)),
        )
    }
}
