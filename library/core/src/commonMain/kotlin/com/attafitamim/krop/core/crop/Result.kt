package com.attafitamim.krop.core.crop

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.getDecodeParams
import com.attafitamim.krop.core.utils.atOrigin
import com.attafitamim.krop.core.utils.coerceAtMost
import com.attafitamim.krop.core.utils.roundUp
import com.attafitamim.krop.core.utils.times
import com.attafitamim.krop.core.utils.viewMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Creates an [ImageBitmap] using the parameters in [cropState].
 * If [maxSize] is not null, the result will be scaled down to match it.
 * Returns null if the image could not be created.
 */
suspend fun CropState.createResult(
    maxSize: IntSize?
): ImageBitmap? = withContext(Dispatchers.Default) {
    runCatching { doCreateResult(maxSize) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
}

/**
 * Renders the crop described by [cropRegion] onto [src] **off-screen** — the
 * non-interactive counterpart of [ImageCropper.cropRegion]. Produces the cropped
 * image on demand from a persisted [CropRegion] (non-destructive cropping) via
 * the same pipeline the interactive cropper uses, so the result matches what the
 * user saw. [maxSize] scales the result down if provided. Returns null if the
 * image could not be decoded.
 */
suspend fun renderCropRegion(
    src: ImageSrc,
    cropRegion: CropRegion,
    maxSize: IntSize? = DefaultMaxCropSize,
): ImageBitmap? {
    val state = cropState(src)
    // Order matters: the region setter constrains against the transformed image
    // bounds, so apply the transform first.
    state.transform = cropRegion.transform
    state.region = cropRegion.region
    return state.createResult(maxSize)
}

/** [renderCropRegion] overload sourcing from an in-memory [bmp]. */
suspend fun renderCropRegion(
    bmp: ImageBitmap,
    cropRegion: CropRegion,
    maxSize: IntSize? = DefaultMaxCropSize,
): ImageBitmap? = renderCropRegion(ImageBitmapSrc(bmp), cropRegion, maxSize)

suspend fun CropState.doCreateResult(maxSize: IntSize?): ImageBitmap? {
    val finalSize = region.size
        .coerceAtMost(maxSize?.toSize())
        .roundUp()
    val result = ImageBitmap(finalSize.width, finalSize.height)
    val canvas = Canvas(result)
    val viewMat = viewMat()
    viewMat.snapFit(region, finalSize.toSize().toRect())
    val imgMat = transform.asMatrix(src.size)
    val totalMat = imgMat * viewMat.matrix

    if (clipResultToShape) {
        canvas.clipPath(shape.asPath(region.atOrigin(maxSize)))
    }

    canvas.concat(totalMat)
    val inParams = getDecodeParams(view = finalSize, img = src.size, totalMat)
        ?: return null
    val decoded = src.open(inParams) ?: return null
    val paint = Paint().apply { filterQuality = FilterQuality.High }
    canvas.drawImageRect(
        image = decoded.bmp, paint = paint,
        dstOffset = decoded.params.subset.topLeft,
        dstSize = decoded.params.subset.size,
    )
    return result
}
