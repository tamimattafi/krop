@file:OptIn(ExperimentalComposeUiApi::class)

package com.attafitamim.krop.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize

@Composable
actual fun rememberScreenSize(): Size =
    LocalWindowInfo.current.containerSize.toSize()

actual fun Modifier.disabledSystemGestureArea(
    exclusion: (LayoutCoordinates) -> Rect
): Modifier = this