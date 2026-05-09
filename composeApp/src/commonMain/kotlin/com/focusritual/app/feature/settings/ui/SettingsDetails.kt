package com.focusritual.app.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.Spacing
import com.focusritual.app.feature.settings.SettingsDetail
import com.focusritual.app.feature.settings.domain.SoundCredit
import com.focusritual.app.feature.settings.domain.soundCredits
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.back
import focusritual.composeapp.generated.resources.settings_credits_author
import focusritual.composeapp.generated.resources.settings_credits_subtitle
import focusritual.composeapp.generated.resources.settings_legal_privacy
import focusritual.composeapp.generated.resources.settings_legal_privacy_activity_body
import focusritual.composeapp.generated.resources.settings_legal_privacy_activity_title
import focusritual.composeapp.generated.resources.settings_legal_privacy_platform_body
import focusritual.composeapp.generated.resources.settings_legal_privacy_platform_title
import focusritual.composeapp.generated.resources.settings_legal_privacy_production_body
import focusritual.composeapp.generated.resources.settings_legal_privacy_production_title
import focusritual.composeapp.generated.resources.settings_legal_privacy_storage_body
import focusritual.composeapp.generated.resources.settings_legal_privacy_storage_title
import focusritual.composeapp.generated.resources.settings_legal_privacy_subtitle
import focusritual.composeapp.generated.resources.settings_legal_privacy_support_body
import focusritual.composeapp.generated.resources.settings_legal_privacy_support_title
import focusritual.composeapp.generated.resources.settings_legal_terms
import focusritual.composeapp.generated.resources.settings_legal_terms_audio_body
import focusritual.composeapp.generated.resources.settings_legal_terms_audio_title
import focusritual.composeapp.generated.resources.settings_legal_terms_personal_body
import focusritual.composeapp.generated.resources.settings_legal_terms_personal_title
import focusritual.composeapp.generated.resources.settings_legal_terms_platform_body
import focusritual.composeapp.generated.resources.settings_legal_terms_platform_title
import focusritual.composeapp.generated.resources.settings_legal_terms_production_body
import focusritual.composeapp.generated.resources.settings_legal_terms_production_title
import focusritual.composeapp.generated.resources.settings_legal_terms_subtitle
import focusritual.composeapp.generated.resources.settings_legal_terms_advice_body
import focusritual.composeapp.generated.resources.settings_legal_terms_advice_title
import focusritual.composeapp.generated.resources.settings_support_sound_credits
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsDetailContent(
    detail: SettingsDetail,
    listState: LazyListState,
    onBack: () -> Unit,
) {
    when (detail) {
        SettingsDetail.SoundCredits -> SoundCreditsDetail(
            listState = listState,
            onBack = onBack,
        )
        SettingsDetail.PrivacyPolicy -> LegalTextDetail(
            title = stringResource(Res.string.settings_legal_privacy),
            subtitle = stringResource(Res.string.settings_legal_privacy_subtitle),
            sections = privacyPolicySections(),
            listState = listState,
            onBack = onBack,
        )
        SettingsDetail.TermsOfUse -> LegalTextDetail(
            title = stringResource(Res.string.settings_legal_terms),
            subtitle = stringResource(Res.string.settings_legal_terms_subtitle),
            sections = termsOfUseSections(),
            listState = listState,
            onBack = onBack,
        )
    }
}

@Composable
private fun SoundCreditsDetail(
    listState: LazyListState,
    onBack: () -> Unit,
) {
    val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    Column(modifier = Modifier.fillMaxSize()) {
        DetailHeader(
            title = stringResource(Res.string.settings_support_sound_credits),
            subtitle = stringResource(Res.string.settings_credits_subtitle),
            onBack = onBack,
        )
        val credits = soundCredits()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 0.dp,
                end = 20.dp,
                bottom = 20.dp + bottomInset,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(credits, key = { it.name }) { SoundCreditCard(it) }
        }
    }
}

@Composable
private fun LegalTextDetail(
    title: String,
    subtitle: String,
    sections: List<LegalSection>,
    listState: LazyListState,
    onBack: () -> Unit,
) {
    val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    Column(modifier = Modifier.fillMaxSize()) {
        DetailHeader(title = title, subtitle = subtitle, onBack = onBack)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 0.dp,
                end = 20.dp,
                bottom = 20.dp + bottomInset,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sections, key = { it.title }) { section ->
                LegalSectionCard(section)
            }
        }
    }
}

@Composable
private fun DetailHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.md, top = Spacing.sm, end = 20.dp, bottom = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GhostIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), onBack)
        Spacer(Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.44f),
            )
        }
    }
}

@Composable
private fun LegalSectionCard(section: LegalSection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.26f))
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f),
                RoundedCornerShape(18.dp),
            )
            .padding(start = Spacing.lg, top = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
    ) {
        Text(
            section.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            section.body,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun SoundCreditCard(credit: SoundCredit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.26f))
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f),
                RoundedCornerShape(18.dp),
            )
            .padding(start = Spacing.lg, top = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.055f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                credit.name.toSoundIcon(),
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.44f),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(Spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                credit.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                credit.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(9.dp))
            Text(
                stringResource(Res.string.settings_credits_author, credit.author),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.44f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                credit.license,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f),
            )
        }
    }
}

private data class LegalSection(
    val title: String,
    val body: String,
)

@Composable
private fun privacyPolicySections(): List<LegalSection> = listOf(
    LegalSection(
        title = stringResource(Res.string.settings_legal_privacy_storage_title),
        body = stringResource(Res.string.settings_legal_privacy_storage_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_privacy_activity_title),
        body = stringResource(Res.string.settings_legal_privacy_activity_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_privacy_platform_title),
        body = stringResource(Res.string.settings_legal_privacy_platform_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_privacy_support_title),
        body = stringResource(Res.string.settings_legal_privacy_support_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_privacy_production_title),
        body = stringResource(Res.string.settings_legal_privacy_production_body),
    ),
)

@Composable
private fun termsOfUseSections(): List<LegalSection> = listOf(
    LegalSection(
        title = stringResource(Res.string.settings_legal_terms_personal_title),
        body = stringResource(Res.string.settings_legal_terms_personal_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_terms_advice_title),
        body = stringResource(Res.string.settings_legal_terms_advice_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_terms_audio_title),
        body = stringResource(Res.string.settings_legal_terms_audio_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_terms_platform_title),
        body = stringResource(Res.string.settings_legal_terms_platform_body),
    ),
    LegalSection(
        title = stringResource(Res.string.settings_legal_terms_production_title),
        body = stringResource(Res.string.settings_legal_terms_production_body),
    ),
)

private fun String.toSoundIcon(): ImageVector = when (this) {
    "Rain" -> Icons.Filled.WaterDrop
    "Thunder" -> Icons.Filled.Thunderstorm
    "Wind" -> Icons.Filled.Air
    "Forest" -> Icons.Filled.Forest
    "Stream" -> Icons.Filled.Water
    "Cafe" -> Icons.Filled.LocalCafe
    "Fireplace" -> Icons.Filled.Fireplace
    "Brown Noise" -> Icons.Filled.GraphicEq
    "Waves" -> Icons.Filled.Waves
    else -> Icons.Filled.GraphicEq
}
