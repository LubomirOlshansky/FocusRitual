package com.focusritual.app.feature.mixer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.displayName

@Composable
internal fun SectionHeader(
    category: SoundCategory,
    activeCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp, start = 26.dp, end = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 0.12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f),
                    RoundedCornerShape(10.dp),
                )
                .padding(start = 8.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
        ) {
            Text(
                text = "$activeCount active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            )
        }
    }
}
