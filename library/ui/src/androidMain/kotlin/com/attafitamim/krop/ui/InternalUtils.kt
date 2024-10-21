package com.attafitamim.krop.ui

import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
actual fun rememberScreenSize(): Size {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val width = configuration.screenWidthDp
    val height = configuration.screenHeightDp

    return remember(density, width, height) {
        with(density) {
            val widthPx = width.dp.toPx()
            val heightPx = height.dp.toPx()
            Size(widthPx, heightPx)
        }
    }
}

actual fun Modifier.disabledSystemGestureArea(
    exclusion: (LayoutCoordinates) -> Rect
): Modifier = systemGestureExclusion(exclusion)
