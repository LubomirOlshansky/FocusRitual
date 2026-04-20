package com.focusritual.app.feature.mixer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.displayName

@Composable
internal fun CategoryPillRow(
    selectedCategory: SoundCategory,
    onSelectCategory: (SoundCategory) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
    ) {
        items(SoundCategory.entries.size) { index ->
            val category = SoundCategory.entries[index]
            CategoryPill(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onSelectCategory(category) },
            )
        }
    }
}

@Composable
private fun CategoryPill(
    category: SoundCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
        },
        animationSpec = tween(300),
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
        },
        animationSpec = tween(300),
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
        },
        animationSpec = tween(300),
    )

    val pillShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .height(40.dp)
            .border(0.5.dp, borderColor, pillShape)
            .background(bgColor, pillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(start = 18.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelected) {
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryFixed,
                        CircleShape,
                    ),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}
