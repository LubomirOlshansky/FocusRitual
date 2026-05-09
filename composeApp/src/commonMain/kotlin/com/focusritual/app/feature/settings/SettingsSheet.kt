package com.focusritual.app.feature.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.app.navigation.EdgeSwipeBackHandler
import com.focusritual.app.core.designsystem.component.CloseButton
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.core.designsystem.theme.Spacing
import com.focusritual.app.core.platformaction.LocalPlatformActions
import com.focusritual.app.feature.settings.ui.AppIdentityBlock
import com.focusritual.app.feature.settings.ui.SettingsDetailContent
import com.focusritual.app.feature.settings.ui.SettingsFrame
import com.focusritual.app.feature.settings.ui.SettingsHome
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val platformActions = LocalPlatformActions.current
    val appVersion = platformActions.appVersion
    val currentLanguageName = platformActions.currentLanguageName

    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.onIntent(SettingsIntent.ResetToHome)
        }
    }

    LaunchedEffect(viewModel, platformActions, appVersion) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.OpenLanguageSettings -> platformActions.openLanguageSettings()
                SettingsEffect.RateApp -> platformActions.rateApp()
                SettingsEffect.ShareApp -> platformActions.shareApp()
                is SettingsEffect.ContactSupport -> platformActions.sendEmail(
                    to = effect.email.to,
                    subject = effect.email.subject,
                    body = effect.email.body(appVersion),
                )
            }
        }
    }

    val navigateBack: () -> Unit = {
        if (uiState.activeDetail != null) {
            viewModel.onIntent(SettingsIntent.CloseDetail)
        } else {
            onDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ModalScrim(
            isVisible = isVisible && uiState.activeDetail == null,
            onDismiss = onDismiss,
        )
        ModalContent(
            isVisible = isVisible,
            uiState = uiState,
            appVersion = appVersion,
            currentLanguageName = currentLanguageName,
            onIntent = viewModel::onIntent,
            onDismiss = onDismiss,
            onNavigateBack = navigateBack,
        )
    }
}

@Composable
private fun ModalScrim(
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.60f),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss() },
        )
    }
}

@Composable
private fun ModalContent(
    isVisible: Boolean,
    uiState: SettingsUiState,
    appVersion: String,
    currentLanguageName: String,
    onIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val settingsListState = rememberLazyListState()
    val detailListState = rememberLazyListState()
    val density = LocalDensity.current

    LaunchedEffect(uiState.activeDetail) {
        if (uiState.activeDetail != null) {
            detailListState.scrollToItem(0)
        }
    }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = tween(durationMillis = 200, easing = FocusRitualEasing.DeepEaseOut),
    )

    val dismissThreshold = with(density) { 88.dp.toPx() }
    val hasDetail = uiState.activeDetail != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = FocusRitualEasing.DeepEaseOut)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 400, easing = FocusRitualEasing.DeepEaseOut),
                initialOffsetY = { fullHeight -> fullHeight },
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FocusRitualEasing.CinematicIn)) +
            slideOutVertically(
                animationSpec = tween(durationMillis = 300, easing = FocusRitualEasing.CinematicIn),
                targetOffsetY = { fullHeight -> fullHeight },
            ),
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = animatedOffset }
                .background(surfaceColor),
        ) {
            SettingsFrame {
                AnimatedContent(
                    targetState = uiState.activeDetail,
                    transitionSpec = {
                        if (targetState != null) {
                            (slideInHorizontally { it / 3 } + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut(tween(150)))
                        } else {
                            (slideInHorizontally { -it / 3 } + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally { it / 3 } + fadeOut(tween(150)))
                        }
                    },
                    label = "settings_content",
                ) { activeDetail ->
                    if (activeDetail != null) {
                        SettingsDetailContent(
                            detail = activeDetail,
                            listState = detailListState,
                            onBack = { onIntent(SettingsIntent.CloseDetail) },
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            SettingsModalHeader(
                                appVersion = appVersion,
                                onClose = onDismiss,
                                modifier = Modifier.pointerInput(hasDetail) {
                                    if (!hasDetail) {
                                        detectVerticalDragGestures(
                                            onDragEnd = {
                                                if (dragOffset > dismissThreshold) {
                                                    onDismiss()
                                                }
                                                dragOffset = 0f
                                            },
                                            onDragCancel = { dragOffset = 0f },
                                            onVerticalDrag = { _, dragAmount ->
                                                if (dragAmount > 0) {
                                                    dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                                                }
                                            },
                                        )
                                    }
                                },
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                SettingsHome(
                                    uiState = uiState,
                                    currentLanguageName = currentLanguageName,
                                    onIntent = onIntent,
                                    listState = settingsListState,
                                )
                            }
                        }
                    }
                }
            }

            EdgeSwipeBackHandler(enabled = true, onBack = onNavigateBack)
        }
    }
}

@Composable
private fun SettingsModalHeader(
    appVersion: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = topInset + Spacing.lg, end = 20.dp, bottom = Spacing.sm),
    ) {
        // Drag indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.settings_title).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
            )
            Spacer(Modifier.weight(1f))
            CloseButton(onClick = onClose)
        }
        AppIdentityBlock(versionName = appVersion)
    }
}
