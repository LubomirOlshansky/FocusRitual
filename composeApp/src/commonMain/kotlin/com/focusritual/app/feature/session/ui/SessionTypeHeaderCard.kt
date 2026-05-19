package com.focusritual.app.feature.session.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.feature.session.SessionMode
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.session_focus_subtitle
import focusritual.composeapp.generated.resources.session_focus_title
import focusritual.composeapp.generated.resources.session_sleep_subtitle
import focusritual.composeapp.generated.resources.session_sleep_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionTypeHeaderCard(
    mode: SessionMode,
    modifier: Modifier = Modifier,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(onSurface.copy(alpha = 0.06f))
            .border(
                width = 0.5.dp,
                color = onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crossfade(
            targetState = mode,
            animationSpec = tween(300),
            label = "headerIcon",
        ) { current ->
            Icon(
                imageVector = when (current) {
                    SessionMode.Focus -> Icons.Outlined.Timer
                    SessionMode.Sleep -> Icons.Outlined.Bedtime
                },
                contentDescription = null,
                tint = onSurface.copy(alpha = 0.65f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Crossfade(
            targetState = mode,
            animationSpec = tween(300),
            label = "headerText",
        ) { current ->
            Column {
                Text(
                    text = when (current) {
                        SessionMode.Focus -> stringResource(Res.string.session_focus_title).uppercase()
                        SessionMode.Sleep -> stringResource(Res.string.session_sleep_title).uppercase()
                    },
                    fontSize = 12.sp,
                    letterSpacing = 0.12.em,
                    fontWeight = FontWeight.Normal,
                    color = onSurface.copy(alpha = 0.85f),
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = when (current) {
                        SessionMode.Focus -> stringResource(Res.string.session_focus_subtitle)
                        SessionMode.Sleep -> stringResource(Res.string.session_sleep_subtitle)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = onSurface.copy(alpha = 0.48f),
                )
            }
        }
    }
}
