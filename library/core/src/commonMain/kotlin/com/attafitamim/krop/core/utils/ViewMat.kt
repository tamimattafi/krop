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
    fun zoom(center: Offset, scale: Float)
    suspend fun fit(inner: Rect, outer: Rect)
    fun snapFit(inner: Rect, outer: Rect)
    val matrix: Matrix
    val invMatrix: Matrix
    val scale: Float
}

fun viewMat() = object : ViewMat {
    var c0 = Offset.Zero
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
        c0 = center
    }

    override fun zoom(center: Offset, scale: Float) {
        val s = Matrix().apply {
            translate(center.x - c0.x, center.y - c0.y)
            translate(center.x, center.y)
            scale(scale, scale)
            translate(-center.x, -center.y)
        }
        update { it *= s }
        c0 = center
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

    private fun getDst(inner: Rect, outer: Rect): Rect? {
        val scale = min(outer.width / inner.width, outer.height / inner.height)
        return Rect(Offset.Zero, inner.size * scale).centerIn(outer)
//        return if(dst.similar(inner)) null else dst
    }

    private fun Rect.similar(other: Rect): Boolean {
        return (intersect(other).area / area) > .95f
    }
}