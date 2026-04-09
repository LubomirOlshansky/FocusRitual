package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateDisabled
import platform.UIKit.UIControlStateHighlighted
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UIImageView
import platform.UIKit.UIView

private fun UIView.clearBackgroundsRecursively() {
    backgroundColor = UIColor.clearColor
    setOpaque(false)
    @Suppress("UNCHECKED_CAST")
    (subviews as List<UIView>).forEach { it.clearBackgroundsRecursively() }
}

private fun UIView.findAllSubviews(): List<UIView> {
    val result = mutableListOf<UIView>()
    fun collect(view: UIView) {
        result += view
        @Suppress("UNCHECKED_CAST")
        (view.subviews as List<UIView>).forEach { collect(it) }
    }
    collect(this)
    return result
}

private fun AVRoutePickerView.styleInternalButton() {
    clearBackgroundsRecursively()

    findAllSubviews()
        .filterIsInstance<UIButton>()
        .forEach { button ->
            button.backgroundColor = UIColor.clearColor
            button.setOpaque(false)
            button.tintColor = UIColor.whiteColor
            button.setBackgroundImage(null, forState = UIControlStateNormal)
            button.setBackgroundImage(null, forState = UIControlStateHighlighted)
            button.setBackgroundImage(null, forState = UIControlStateSelected)
            button.setBackgroundImage(null, forState = UIControlStateDisabled)
            button.setImage(null, forState = UIControlStateNormal)
            button.setImage(null, forState = UIControlStateHighlighted)
            button.setImage(null, forState = UIControlStateSelected)
            button.setImage(null, forState = UIControlStateDisabled)
            button.setTitle("", forState = UIControlStateNormal)
            button.layer.borderWidth = 0.0
            button.layer.shadowOpacity = 0f
            button.clipsToBounds = false
        }

    // Clear any standalone UIImageView backgrounds
    findAllSubviews()
        .filterIsInstance<UIImageView>()
        .forEach { imageView ->
            imageView.backgroundColor = UIColor.clearColor
            imageView.setOpaque(false)
        }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AirPlayButton(
    modifier: Modifier
) {
    UIKitView(
        modifier = modifier,
        factory = {
            AVRoutePickerView().apply {
                prioritizesVideoDevices = false
                backgroundColor = UIColor.clearColor
                tintColor = UIColor.whiteColor
                activeTintColor = UIColor.whiteColor
                setOpaque(false)
                clipsToBounds = false
                styleInternalButton()
            }
        },
        update = { view ->
            view.backgroundColor = UIColor.clearColor
            view.tintColor = UIColor.whiteColor
            view.activeTintColor = UIColor.whiteColor
            view.setOpaque(false)
            view.clipsToBounds = false
            view.styleInternalButton()
        }
    )
}