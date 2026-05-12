package com.focusritual.app.feature.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.app.navigation.EdgeSwipeBackHandler
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.core.designsystem.theme.Spacing
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.core.platformaction.LocalPlatformActions
import com.focusritual.app.feature.settings.ui.SettingsDetailContent
import com.focusritual.app.feature.settings.ui.SettingsFrame
import com.focusritual.app.feature.settings.ui.SettingsHome
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.close
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hapticController: HapticController = HapticController(),
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

    Box(modifier = modifier.fillMaxSize()) {
        ModalContent(
            isVisible = isVisible,
            uiState = uiState,
            appVersion = appVersion,
            currentLanguageName = currentLanguageName,
            hapticController = hapticController,
            onIntent = viewModel::onIntent,
            onDismiss = onDismiss,
            onOpenUrl = platformActions::openUrl,
        )
    }
}

@Composable
private fun ModalContent(
    isVisible: Boolean,
    uiState: SettingsUiState,
    appVersion: String,
    currentLanguageName: String,
    hapticController: HapticController,
    onIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val scope = rememberCoroutineScope()
    val settingsListState = rememberLazyListState()
    val detailListState = rememberLazyListState()
    val density = LocalDensity.current

    LaunchedEffect(uiState.activeDetail) {
        if (uiState.activeDetail != null) {
            detailListState.scrollToItem(0)
        }
    }

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var sheetHeightPx by remember { mutableFloatStateOf(1f) }
    var isClosing by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            settleJob?.cancel()
            settleJob = null
            isClosing = false
            dragOffsetY = 0f
        }
    }

    val dismissThreshold = with(density) { 120.dp.toPx() }
    val hasDetail = uiState.activeDetail != null
    val activeListState: LazyListState = if (hasDetail) detailListState else settingsListState

    val settleSheet: () -> Unit = {
        if (!isClosing && dragOffsetY > 0f) {
            settleJob?.cancel()
            settleJob = scope.launch {
                val animation = Animatable(dragOffsetY.coerceAtLeast(0f))
                animation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 280, easing = FocusRitualEasing.DeepEaseOut),
                ) {
                    dragOffsetY = value
                }
                dragOffsetY = 0f
            }
        }
    }
    val closeSheet: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            settleJob?.cancel()
            settleJob = scope.launch {
                val animation = Animatable(dragOffsetY.coerceAtLeast(0f))
                animation.animateTo(
                    targetValue = sheetHeightPx.coerceAtLeast(1f),
                    animationSpec = tween(durationMillis = 300, easing = FocusRitualEasing.CinematicIn),
                ) {
                    dragOffsetY = value
                }
                latestOnDismiss()
                isClosing = false
            }
        }
    }
    val finishDrag: () -> Unit = {
        if (dragOffsetY > dismissThreshold) {
            closeSheet()
        } else {
            settleSheet()
        }
    }

    val nestedScrollConnection = remember(activeListState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val atTop = activeListState.firstVisibleItemIndex == 0 &&
                    activeListState.firstVisibleItemScrollOffset == 0
                return when {
                    isClosing -> Offset.Zero
                    available.y > 0f && atTop -> {
                        settleJob?.cancel()
                        settleJob = null
                        dragOffsetY = (dragOffsetY + available.y).coerceAtLeast(0f)
                        available
                    }
                    dragOffsetY > 0f && available.y < 0f -> {
                        settleJob?.cancel()
                        settleJob = null
                        val consumedY = available.y.coerceAtLeast(-dragOffsetY)
                        dragOffsetY = (dragOffsetY + consumedY).coerceAtLeast(0f)
                        Offset(0f, consumedY)
                    }
                    else -> Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (dragOffsetY > 0f) {
                    if (dragOffsetY > dismissThreshold || available.y > 800f) {
                        closeSheet()
                    } else {
                        settleSheet()
                    }
                    available
                } else {
                    Velocity.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (dragOffsetY > 0f && !isClosing) settleSheet()
                return Velocity.Zero
            }
        }
    }

    val headerDragModifier = Modifier.pointerInput(isClosing) {
        detectVerticalDragGestures(
            onDragEnd = finishDrag,
            onDragCancel = settleSheet,
            onVerticalDrag = { _, dragAmount ->
                if (!isClosing && (dragAmount > 0f || dragOffsetY > 0f)) {
                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                }
            },
        )
    }

    SettingsBackHandler(
        enabled = isVisible,
        onBack = {
            if (hasDetail) {
                onIntent(SettingsIntent.CloseDetail)
            } else {
                closeSheet()
            }
        },
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 480, easing = FocusRitualEasing.Ritual),
            initialOffsetY = { it },
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FocusRitualEasing.Atmospheric)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 340, easing = FocusRitualEasing.CinematicIn),
            targetOffsetY = { it },
        ) + fadeOut(animationSpec = tween(durationMillis = 260, easing = FocusRitualEasing.CinematicIn)),
        modifier = Modifier.fillMaxSize(),
        label = "settingsSheet",
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxSheetHeight = maxHeight * 0.90f
            val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.60f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { closeSheet() },
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(maxSheetHeight)
                    .onSizeChanged { sheetHeightPx = it.height.toFloat() }
                    .offset { IntOffset(0, dragOffsetY.roundToInt().coerceAtLeast(0)) }
                    .clip(sheetShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f),
                        shape = sheetShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { },
            ) {
                SettingsFrame(modifier = Modifier.fillMaxSize()) {
                    SettingsModalHeader(
                        onClose = closeSheet,
                        modifier = headerDragModifier,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection),
                    ) {
                        AnimatedContent(
                            targetState = uiState.activeDetail,
                            transitionSpec = {
                                if (targetState != null) {
                                    (slideInHorizontally(
                                        animationSpec = tween(
                                            durationMillis = 320,
                                            easing = FocusRitualEasing.DeepEaseOut,
                                        ),
                                    ) { it / 3 } + fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FocusRitualEasing.DeepEaseOut,
                                        ),
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(
                                                durationMillis = 260,
                                                easing = FocusRitualEasing.CinematicIn,
                                            ),
                                        ) { -it / 3 } + fadeOut(
                                            animationSpec = tween(
                                                durationMillis = 260,
                                                easing = FocusRitualEasing.CinematicIn,
                                            ),
                                        ),
                                    )
                                } else {
                                    (slideInHorizontally(
                                        animationSpec = tween(
                                            durationMillis = 320,
                                            easing = FocusRitualEasing.DeepEaseOut,
                                        ),
                                    ) { -it / 3 } + fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FocusRitualEasing.DeepEaseOut,
                                        ),
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(
                                                durationMillis = 260,
                                                easing = FocusRitualEasing.CinematicIn,
                                            ),
                                        ) { it / 3 } + fadeOut(
                                            animationSpec = tween(
                                                durationMillis = 260,
                                                easing = FocusRitualEasing.CinematicIn,
                                            ),
                                        ),
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            label = "settings_content",
                        ) { activeDetail ->
                            if (activeDetail != null) {
                                SettingsDetailContent(
                                    detail = activeDetail,
                                    listState = detailListState,
                                    onBack = { onIntent(SettingsIntent.CloseDetail) },
                                    onOpenUrl = onOpenUrl,
                                )
                            } else {
                                SettingsHome(
                                    uiState = uiState,
                                    appVersion = appVersion,
                                    currentLanguageName = currentLanguageName,
                                    hapticController = hapticController,
                                    onIntent = onIntent,
                                    listState = settingsListState,
                                )
                            }
                        }
                    }
                }

                EdgeSwipeBackHandler(
                    enabled = true,
                    onBack = {
                        if (hasDetail) {
                            onIntent(SettingsIntent.CloseDetail)
                        } else {
                            closeSheet()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsModalHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = Spacing.md, end = 20.dp, bottom = Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
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
            SettingsSheetCloseButton(onClick = onClose)
        }
    }
}

@Composable
private fun SettingsSheetCloseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FocusRitualEasing.DeepEaseOut),
    )

    Box(
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                shape = CircleShape,
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.close),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(14.dp),
        )
    }
}
