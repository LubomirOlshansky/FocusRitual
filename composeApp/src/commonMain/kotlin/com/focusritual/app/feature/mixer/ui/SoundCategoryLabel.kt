package com.focusritual.app.feature.mixer.ui

import androidx.compose.runtime.Composable
import com.focusritual.app.feature.mixer.domain.SoundCategory
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.mixer_category_all
import focusritual.composeapp.generated.resources.mixer_category_nature
import focusritual.composeapp.generated.resources.mixer_category_noise
import focusritual.composeapp.generated.resources.mixer_category_places
import focusritual.composeapp.generated.resources.mixer_category_weather
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SoundCategory.localizedLabel(): String = when (this) {
    SoundCategory.ALL -> stringResource(Res.string.mixer_category_all)
    SoundCategory.NATURE -> stringResource(Res.string.mixer_category_nature)
    SoundCategory.WEATHER -> stringResource(Res.string.mixer_category_weather)
    SoundCategory.PLACES -> stringResource(Res.string.mixer_category_places)
    SoundCategory.NOISE -> stringResource(Res.string.mixer_category_noise)
}
