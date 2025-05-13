package com.attafitamim.krop.core.crop

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.images.ImageStream
import com.attafitamim.krop.core.images.ImageStreamSrc
import com.attafitamim.krop.core.images.toImageSrc
import java.io.File

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [file] will be used as a source.
 */
suspend fun ImageCropper.crop(
    file: File,
    maxResultSize: IntSize? = DefaultMaxCropSize,
): CropResult = crop(maxResultSize) { file.toImageSrc() }


/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [dataSource] will be used as a source.
 */
suspend fun ImageCropper.crop(
    dataSource: ImageStream,
    maxResultSize: IntSize? = DefaultMaxCropSize,
): CropResult = crop(maxResultSize) { ImageStreamSrc.invoke(dataSource) }