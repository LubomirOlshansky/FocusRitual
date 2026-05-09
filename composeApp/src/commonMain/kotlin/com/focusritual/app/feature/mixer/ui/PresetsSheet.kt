package com.focusritual.app.feature.mixer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.feature.mixer.domain.MixPreset
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.delete
import focusritual.composeapp.generated.resources.presets_all_mixes
import focusritual.composeapp.generated.resources.presets_currently_loaded
import focusritual.composeapp.generated.resources.presets_save_current
import focusritual.composeapp.generated.resources.presets_saved_mixes
import focusritual.composeapp.generated.resources.presets_swipe_left_delete
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun PresetsSheet(
    isVisible: Boolean,
    presets: List<MixPreset>,
    loadedPresetId: String?,
    isDirtyFromPreset: Boolean,
    soundNamesById: Map<String, String>,
    onLoad: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    val scope = rememberCoroutineScope()
    var sessionKey by remember { mutableIntStateOf(0) }
    var loadedPulseToken by remember { mutableIntStateOf(0) }
    var lastLoadedPulseId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var sheetHeightPx by remember { mutableFloatStateOf(1f) }
    var isClosing by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    val loadedPreset = remember(presets, loadedPresetId, isDirtyFromPreset) {
        if (isDirtyFromPreset) null else presets.firstOrNull { it.id == loadedPresetId }
    }
    val allPresets = remember(presets, loadedPreset) {
        loadedPreset?.let { loaded -> presets.filterNot { it.id == loaded.id } } ?: presets
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            settleJob?.cancel()
            settleJob = null
            isClosing = false
            dragOffsetY = 0f
            sessionKey++
        }
    }
    LaunchedEffect(isVisible, loadedPresetId, isDirtyFromPreset) {
        if (!isVisible || isDirtyFromPreset) {
            lastLoadedPulseId = null
            return@LaunchedEffect
        }
        if (loadedPresetId != null && loadedPresetId != lastLoadedPulseId) {
            lastLoadedPulseId = loadedPresetId
            loadedPulseToken++
        }
    }

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

    PresetsBackHandler(enabled = isVisible, onBack = closeSheet)

    val listState = rememberLazyListState()
    val nestedScrollConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
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
                    if (dragOffsetY > 120f || available.y > 800f) {
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
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val primary = MaterialTheme.colorScheme.primary

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 480, easing = FocusRitualEasing.Ritual),
            initialOffsetY = { it },
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FocusRitualEasing.Atmospheric)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 340, easing = FocusRitualEasing.CinematicIn),
            targetOffsetY = { it },
        ) + fadeOut(animationSpec = tween(durationMillis = 260)),
        modifier = modifier.fillMaxSize(),
        label = "presetsSheet",
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxSheetHeight = maxHeight * 0.88f

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
                    .wrapContentHeight()
                    .heightIn(max = maxSheetHeight)
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        primary.copy(alpha = 0.16f),
                                        primary.copy(alpha = 0.06f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .nestedScroll(nestedScrollConnection),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item(key = "handle") { SheetHandle() }
                        item(key = "header") {
                            SheetHeader(onSaveCurrent = onSaveCurrent)
                        }
                        item(key = "divider") {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                                thickness = 0.5.dp,
                            )
                        }

                        loadedPreset?.let { preset ->
                            item(key = "currently_loaded_label") {
                                SectionLabel(
                                    text = stringResource(Res.string.presets_currently_loaded).uppercase(),
                                    modifier = Modifier.offset(y = (-4).dp),
                                )
                            }
                            item(key = "loaded_${preset.id}") {
                                PresetCard(
                                    preset = preset,
                                    soundNamesById = soundNamesById,
                                    isLoaded = true,
                                    pulseToken = loadedPulseToken,
                                    onLoad = { },
                                )
                            }
                        }

                        if (allPresets.isNotEmpty()) {
                            item(key = "all_mixes_label") {
                                SectionLabel(
                                    text = stringResource(Res.string.presets_all_mixes).uppercase(),
                                    modifier = Modifier.padding(top = if (loadedPreset == null) 4.dp else 8.dp),
                                )
                            }
                            items(
                                items = allPresets,
                                key = { preset -> "preset_${preset.id}_$sessionKey" },
                            ) { preset ->
                                val onLoadPreset = remember(preset.id, onLoad) {
                                    { onLoad(preset.id) }
                                }
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            onDelete(preset.id)
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = { SwipeDeleteBackground() },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 220),
                                        placementSpec = tween(
                                            durationMillis = 320,
                                            easing = FocusRitualEasing.DeepEaseOut,
                                        ),
                                        fadeOutSpec = tween(durationMillis = 260),
                                    ),
                                ) {
                                    PresetCard(
                                        preset = preset,
                                        soundNamesById = soundNamesById,
                                        isLoaded = false,
                                        onLoad = onLoadPreset,
                                    )
                                }
                            }
                        }

                        if (presets.isNotEmpty()) {
                            item(key = "hint") {
                                Text(
                                    text = stringResource(Res.string.presets_swipe_left_delete),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, bottom = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)),
        )
    }
}

@Composable
private fun SheetHeader(onSaveCurrent: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.presets_saved_mixes).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.16.em,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
        )
        SaveCurrentPill(onClick = onSaveCurrent)
    }
}

@Composable
private fun SaveCurrentPill(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FocusRitualEasing.DeepEaseOut),
        label = "saveCurrentPresetPillScale",
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.60f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
        )
        Text(
            text = stringResource(Res.string.presets_save_current),
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.12.em,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun SwipeDeleteBackground() {
    val shape = RoundedCornerShape(16.dp)
    val tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.88f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 2.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(108.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
                    shape = shape,
                )
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(Res.string.delete),
                modifier = Modifier.size(18.dp),
                tint = tint,
            )
            Text(
                text = stringResource(Res.string.delete),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = tint,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}