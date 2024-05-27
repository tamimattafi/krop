package com.mr0xf00.easycrop.core.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix

val IdentityMat = Matrix()

operator fun Matrix.times(other: Matrix): Matrix = copy().apply {
    this *= other
}

fun Matrix.setScaleTranslate(sx: Float, sy: Float, tx: Float, ty: Float) {
    reset()
    values[Matrix.ScaleX] = sx
    values[Matrix.TranslateX] = tx
    values[Matrix.ScaleY] = sy
    values[Matrix.TranslateY] = ty
}

fun Matrix.setRectToRect(src: Rect, dst: Rect) {
    val sx: Float = dst.width / src.width
    val tx = dst.left - src.left * sx
    val sy: Float = dst.height / src.height
    val ty = dst.top - src.top * sy
    setScaleTranslate(sx, sy, tx, ty)
}

fun Matrix.copy(): Matrix = Matrix(values.clone())

fun Matrix.inverted() = copy().apply { invert() }

fun FloatArray.clone() = FloatArray(size) { index ->
    this[index]
}
