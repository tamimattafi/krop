package com.attafitamim.krop.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates

@Composable
expect fun rememberScreenSize(): Size

expect fun Modifier.disabledSystemGestureArea(
    exclusion: (LayoutCoordinates) -> Rect
): Modifier
