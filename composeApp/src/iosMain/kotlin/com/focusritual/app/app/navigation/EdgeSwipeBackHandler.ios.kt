package com.focusritual.app.app.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val EdgeSwipeTouchWidth = 28.dp
private val HorizontalIntentThreshold = 8.dp
private val VerticalCancelThreshold = 18.dp
private val BackTriggerDistance = 72.dp
private val BackMaxVerticalDrift = 60.dp

@Composable
actual fun EdgeSwipeBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    if (!enabled) return

    val latestOnBack by rememberUpdatedState(onBack)
    val layoutDirection = LocalLayoutDirection.current
    val edgeAlignment = when (layoutDirection) {
        LayoutDirection.Ltr -> Alignment.TopStart
        LayoutDirection.Rtl -> Alignment.TopEnd
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(edgeAlignment)
                .fillMaxHeight()
                .width(EdgeSwipeTouchWidth)
                .pointerInput(layoutDirection) {
                    val horizontalIntentThreshold = HorizontalIntentThreshold.toPx()
                    val verticalCancelThreshold = VerticalCancelThreshold.toPx()
                    val backTriggerDistance = BackTriggerDistance.toPx()
                    val backMaxVerticalDrift = BackMaxVerticalDrift.toPx()

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalX = 0f
                        var totalY = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y

                            val directedX = if (layoutDirection == LayoutDirection.Ltr) totalX else -totalX
                            val horizontalIntent = directedX > horizontalIntentThreshold &&
                                abs(totalX) > abs(totalY) * 1.2f

                            if (abs(totalY) > verticalCancelThreshold && abs(totalY) > abs(totalX)) break
                            if (horizontalIntent) change.consume()
                            if (directedX > backTriggerDistance && abs(totalY) < backMaxVerticalDrift) {
                                latestOnBack()
                                break
                            }
                        }
                    }
                },
        )
    }
}
