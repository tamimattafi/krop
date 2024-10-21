package com.attafitamim.krop.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates

actual fun Modifier.disabledSystemGestureArea(
    exclusion: (LayoutCoordinates) -> Rect
): Modifier = this