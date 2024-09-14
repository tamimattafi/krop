package com.attafitamim.krop.core.crop

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.utils.IdentityMat

/**
 * Transformation applied on an image with [pivotRel] as pivot's relative position.
 */
data class ImgTransform(val angleDeg: Int, val scale: Offset, val pivotRel: Offset) {
    val hasTransform get() = angleDeg != 0 || scale != Offset(1f, 1f)

    companion object {
        @Stable
        val Identity = ImgTransform(0, Offset(1f, 1f), Offset(.5f, .5f))
    }
}

fun ImgTransform.asMatrix(imgSize: IntSize): Matrix {
    if (!hasTransform) return IdentityMat
    val matrix = Matrix()
    val pivot = Offset(imgSize.width * pivotRel.x, imgSize.height * pivotRel.y)
    matrix.translate(pivot.x, pivot.y)
    matrix.rotateZ(angleDeg.toFloat())
    matrix.scale(scale.x, scale.y)
    matrix.translate(-pivot.x, -pivot.y)
    return matrix
}