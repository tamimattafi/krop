package com.mr0xf00.easycrop.core.images

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toSize
import com.mr0xf00.easycrop.core.utils.align
import com.mr0xf00.easycrop.core.utils.inverted
import com.mr0xf00.easycrop.core.utils.containsInclusive
import com.mr0xf00.easycrop.core.utils.roundOut

data class DecodeParams(val sampleSize: Int, val subset: IntRect)
data class DecodeResult(val params: DecodeParams, val bmp: ImageBitmap)

fun calculateSampleSize(imgRegion: IntSize, view: IntSize): Int {
    val imgArea = imgRegion.let { it.width.toDouble() * it.height }
    val viewArea = view.let { it.width.toDouble() * it.height }
    return (imgArea / viewArea).toFloat().align(2).coerceIn(1f, 32f).toInt()
}

fun getImageSubset(
    view: IntSize, viewToImg: Matrix, imgRect: IntRect, align: Boolean
): IntRect {
    return viewToImg
        .map(view.toSize().toRect()).let{ if(align) it.align(128) else it }
        .roundOut().intersect(imgRect)
}

fun getDecodeParams(
    view: IntSize,
    img: IntSize,
    imgToView: Matrix
): DecodeParams? {
    if (view.width <= 0 || view.height <= 0) return null
    val imgRect = img.toIntRect()
    val viewToImg = imgToView.inverted()
    val subset = getImageSubset(view, viewToImg, imgRect, align = true)
    if (subset.isEmpty) return null
    val sampleSize = calculateSampleSize(
        imgRegion = getImageSubset(view, viewToImg, imgRect, align = false).size,
        view = view
    )
    return DecodeParams(sampleSize, subset)
}

fun DecodeParams.contains(other: DecodeParams): Boolean {
    return sampleSize == other.sampleSize &&
            subset.containsInclusive(other.subset)
}