package com.attafitamim.krop.core.utils

import androidx.compose.ui.unit.IntSize

class ZoomLimits(
    originalImageSize: IntSize,
    view: IntSize,
    minCropSize: Float,
) {
    val maxFactor: Float

    init {
        val viewAspectRatio = view.width.toFloat() / view.height
        val imageAspectRatio = originalImageSize.width.toFloat() / originalImageSize.height

        val fullImageSize = if (viewAspectRatio > imageAspectRatio) {
            originalImageSize.height.coerceAtLeast(view.height)
        } else {
            originalImageSize.width.coerceAtLeast(view.width)
        }

        maxFactor = fullImageSize / minCropSize
    }
}