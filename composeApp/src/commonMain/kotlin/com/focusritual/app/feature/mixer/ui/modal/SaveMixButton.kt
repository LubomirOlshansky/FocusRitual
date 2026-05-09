package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.save_this_mix
import focusritual.composeapp.generated.resources.saved
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SaveMixButton(
    isDirty: Boolean,
    alreadySaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetAlpha = if (alreadySaved) 0.35f else 1f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 250),
        label = "saveMixButtonAlpha",
    )

    val label = if (alreadySaved) {
        stringResource(Res.string.saved)
    } else {
        stringResource(Res.string.save_this_mix)
    }
    val icon = if (alreadySaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder

    @Suppress("UNUSED_VARIABLE")
    val dirtyHint = isDirty // reserved for future visual treatment per plan

    ModalActionButton(
        onClick = { if (!alreadySaved) onClick() },
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.50f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f),
        modifier = modifier.alpha(alpha),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}
