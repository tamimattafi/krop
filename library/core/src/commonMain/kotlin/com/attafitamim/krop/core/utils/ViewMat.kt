package com.attafitamim.krop.core.utils

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import kotlin.math.min

@Stable
interface ViewMat {
    fun zoomStart(center: Offset)
    fun zoom(center: Offset, scale: Float, zoomLimits: ZoomLimits)
    fun dragStart(point: Offset)
    fun drag(point: Offset)
    suspend fun fit(inner: Rect, outer: Rect)
    fun setOriginalScale(defaultRegion: Rect, outer: Rect)
    fun snapFit(inner: Rect, outer: Rect)
    val matrix: Matrix
    val invMatrix: Matrix
    val scale: Float
}

fun viewMat() = object : ViewMat {
    private var originalScale: Float = 1f
    var zoomCenter = Offset.Zero
    var dragPoint = Offset.Zero
    var mat by mutableStateOf(Matrix(), neverEqualPolicy())
    val inv by derivedStateOf {
        Matrix().apply {
            setFrom(mat)
            invert()
        }
    }
    override val scale by derivedStateOf {
        mat.values[Matrix.ScaleX]
    }

    override fun zoomStart(center: Offset) {
        zoomCenter = center
    }

    override fun zoom(center: Offset, scale: Float, zoomLimits: ZoomLimits) {
        val s = Matrix().apply {
            translate(center.x - zoomCenter.x, center.y - zoomCenter.y)
            translate(center.x, center.y)

            val currentScale = mat.values[Matrix.ScaleX]
            val desiredNextScale = currentScale * scale

            val allowedScale = when {
                desiredNextScale > zoomLimits.maxFactor -> (zoomLimits.maxFactor / currentScale).coerceAtMost(1f)
                desiredNextScale < originalScale -> (originalScale / currentScale).coerceAtLeast(1f)
                else -> scale
            }
            scale(allowedScale, allowedScale)

            translate(-center.x, -center.y)
        }
        update { it *= s }
        zoomCenter = center
    }

    override fun dragStart(point: Offset) {
        dragPoint = point
    }

    override fun drag(point: Offset) {
        val s = Matrix().apply {
            translate(point.x - dragPoint.x, point.y - dragPoint.y)
        }
        update { it *= s }
        dragPoint = point
    }

    inline fun update(op: (Matrix) -> Unit) {
        mat = mat.copy().also(op)
    }

    override val matrix: Matrix
        get() = mat
    override val invMatrix: Matrix
        get() = inv

    override suspend fun fit(inner: Rect, outer: Rect) {
        val dst = getDst(inner, outer) ?: return
        val mat = Matrix()
        val initial = this.mat.copy()
        animate(0f, 1f) { p, _ ->
            update {
                it.setFrom(initial)
                it *= mat.apply { setRectToRect(inner, inner.lerp(dst, p)) }
            }
        }
    }

    override fun snapFit(inner: Rect, outer: Rect) {
        val dst = getDst(inner, outer) ?: return
        update { it *= Matrix().apply { setRectToRect(inner, dst) } }
    }

    override fun setOriginalScale(defaultRegion: Rect, outer: Rect) {
        val dst = getDst(defaultRegion, outer) ?: return
        val matrix = Matrix().apply { setRectToRect(defaultRegion, dst) }
        originalScale = matrix.values[Matrix.ScaleX]
    }

    private fun getDst(inner: Rect, outer: Rect): Rect? {
        val scale = min(outer.width / inner.width, outer.height / inner.height)
        return Rect(Offset.Zero, inner.size * scale).centerIn(outer)
//        return if(dst.similar(inner)) null else dst
    }

    private fun Rect.similar(other: Rect): Boolean {
        return (intersect(other).area / area) > .95f
    }
}