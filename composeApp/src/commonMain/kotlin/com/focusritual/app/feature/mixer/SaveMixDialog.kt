package com.focusritual.app.feature.mixer

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.feature.mixer.domain.SoundState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SaveDialogState { Input, Saved }

private const val NAME_MAX_LENGTH = 40

@Composable
fun SaveMixDialog(
    activeSounds: List<SoundState>,
    dialogState: SaveDialogState,
    existingMixNames: Set<String>,
    onSave: (name: String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSaving by remember { mutableStateOf(false) }
    val showSaved = isSaving || dialogState == SaveDialogState.Saved
    val safeDismiss: () -> Unit = { if (!showSaved) onDismiss() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.68f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { safeDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                DialogCard(
                    activeSounds = activeSounds,
                    existingMixNames = existingMixNames,
                    showSaved = showSaved,
                    onSubmit = { name ->
                        isSaving = true
                        onSave(name)
                    },
                    onDone = onDone,
                    onCancel = safeDismiss,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun DialogCard(
    activeSounds: List<SoundState>,
    existingMixNames: Set<String>,
    showSaved: Boolean,
    onSubmit: (name: String) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF191E25))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                        ),
                    ),
                ),
        )

        var savedContentHeight by remember { mutableStateOf(0) }
        val density = LocalDensity.current

        Crossfade(
            targetState = showSaved,
            animationSpec = tween(durationMillis = 280, easing = FocusRitualEasing.DeepEaseOut),
            label = "saveMixDialogContent",
        ) { saved ->
            if (saved) {
                val animationHeightDp = with(density) { savedContentHeight.toDp() }
                SavedAnimation(
                    modifier = Modifier.fillMaxWidth().height(animationHeightDp.coerceAtLeast(200.dp)),
                    onDone = onDone,
                )
            } else {
                Box(modifier = Modifier.onSizeChanged { savedContentHeight = it.height }) {
                    InputContent(
                        activeSounds = activeSounds,
                        existingMixNames = existingMixNames,
                        onSubmit = onSubmit,
                        onCancel = onCancel,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputContent(
    activeSounds: List<SoundState>,
    existingMixNames: Set<String>,
    onSubmit: (name: String) -> Unit,
    onCancel: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val normalizedExistingMixNames = remember(existingMixNames) {
        existingMixNames.map { it.trim().lowercase() }.toSet()
    }
    val trimmedName = name.trim()
    val nameCollides = trimmedName.isNotEmpty() && normalizedExistingMixNames.contains(trimmedName.lowercase())
    val canSave = trimmedName.isNotEmpty() && !nameCollides
    val focusRequester = remember { FocusRequester() }
    val helperText = if (nameCollides) {
        "A mix with this name already exists."
    } else {
        "Saved mixes appear on the mixer screen."
    }
    val helperFontSize = if (nameCollides) 9.sp else 10.sp
    val helperLineHeight = if (nameCollides) 13.5.sp else 14.5.sp

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun submit() {
        if (!canSave) return
        onSubmit(trimmedName)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Save this mix",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.02).em,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Name your sanctuary",
            fontSize = 10.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            activeSounds.forEach { sound ->
                SoundPill(sound = sound)
            }
        }

        Spacer(Modifier.height(18.dp))

        MixNameInput(
            value = name,
            onValueChange = { newValue ->
                if (newValue.length <= NAME_MAX_LENGTH) {
                    name = newValue
                }
            },
            focusRequester = focusRequester,
            onImeDone = ::submit,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = helperText,
            modifier = Modifier.fillMaxWidth(),
            fontSize = helperFontSize,
            lineHeight = helperLineHeight,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = if (nameCollides) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
            },
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)),
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CancelActionButton(
                modifier = Modifier.weight(1f),
                onClick = onCancel,
            )
            SaveActionButton(
                modifier = Modifier.weight(1f),
                canSave = canSave,
                onClick = ::submit,
            )
        }
    }
}


@Composable
private fun SavedAnimation(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val ringSizes = listOf(64.dp, 96.dp, 128.dp)
    val targetAlphas = listOf(0.22f, 0.12f, 0.06f)
    val ringScales = List(3) { remember { Animatable(0.6f) } }
    val ringAlphas = List(3) { remember { Animatable(0f) } }
    val checkScale = remember { Animatable(0.7f) }
    val checkAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            ringScales.forEachIndexed { index, scale ->
                launch {
                    delay(index * 60L)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 520,
                            easing = FocusRitualEasing.Atmospheric,
                        ),
                    )
                }
                launch {
                    delay(index * 60L)
                    ringAlphas[index].animateTo(
                        targetValue = targetAlphas[index],
                        animationSpec = tween(
                            durationMillis = 520,
                            easing = FocusRitualEasing.Atmospheric,
                        ),
                    )
                }
            }
            launch {
                checkAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 360,
                        delayMillis = 180,
                        easing = FocusRitualEasing.DeepEaseOut,
                    ),
                )
            }
            launch {
                checkScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 360,
                        delayMillis = 180,
                        easing = FocusRitualEasing.DeepEaseOut,
                    ),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(900L)
        onDone()
    }

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ringSizes.forEachIndexed { index, ringSize ->
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .graphicsLayer {
                        scaleX = ringScales[index].value
                        scaleY = ringScales[index].value
                    }
                    .border(
                        width = 0.5.dp,
                        color = primary.copy(alpha = ringAlphas[index].value),
                        shape = CircleShape,
                    ),
            )
        }
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = checkScale.value
                    scaleY = checkScale.value
                    alpha = checkAlpha.value
                },
            tint = primary.copy(alpha = 0.92f),
        )
    }
}

@Composable
private fun MixNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onImeDone: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val glowBackground by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "mixNameGlowBackground",
    )
    val glowBorder by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.06f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "mixNameGlowBorder",
    )
    val fieldBorder by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "mixNameFieldBorder",
    )
    val inputTextStyle = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
    )

    val glowShape = RoundedCornerShape(12.dp)
    val fieldShape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(glowShape)
            .background(glowBackground)
            .border(width = 0.5.dp, color = glowBorder, shape = glowShape)
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .border(width = 0.5.dp, color = fieldBorder, shape = fieldShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
                singleLine = true,
                textStyle = inputTextStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
                keyboardActions = KeyboardActions(onDone = { onImeDone() }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "e.g. Late night rain",
                                style = inputTextStyle.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f),
                                ),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
private fun SoundPill(sound: SoundState) {
    val shape = RoundedCornerShape(999.dp)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
                shape = shape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = sound.icon,
            contentDescription = null,
            modifier = Modifier.size(8.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = sound.name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f),
        )
    }
}

@Composable
private fun SaveActionButton(
    modifier: Modifier = Modifier,
    canSave: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (canSave) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "saveButtonBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = if (canSave) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "saveButtonBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (canSave) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "saveButtonContent",
    )

    val shape = RoundedCornerShape(999.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(width = 0.5.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = canSave,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (canSave) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                            ),
                        ),
                    ),
            )
        }

        Text(
            text = "Save",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = contentColor,
        )
    }
}

@Composable
private fun CancelActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Cancel",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
        )
    }
}
