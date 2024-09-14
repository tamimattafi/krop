package com.attafitamim.krop.core.utils

import androidx.compose.ui.graphics.Path

fun polygonPath(
    tx: Float = 0f, ty: Float = 0f,
    sx: Float = 1f, sy: Float = 1f,
    points: FloatArray
): Path = Path().apply {
    if (points.size < 2) return@apply
    moveTo(points[0] * sx + tx, points[1] * sy + ty)
    for (i in 1 until points.size / 2) {
        lineTo(points[(i * 2) + 0] * sx + tx, points[(i * 2) + 1] * sy + ty)
    }
    close()
}
