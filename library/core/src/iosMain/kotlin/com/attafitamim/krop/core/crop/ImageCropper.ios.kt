package com.attafitamim.krop.core.crop

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.images.NSURLImageSrc
import com.attafitamim.krop.core.images.PHAssetImageSrc
import com.attafitamim.krop.core.images.UIImageSrc
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Photos.PHAsset
import platform.UIKit.UIImage

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [uiImage] will be used as a source.
 */
@OptIn(ExperimentalForeignApi::class)
suspend fun ImageCropper.crop(
    uiImage: UIImage,
    maxResultSize: IntSize? = DefaultMaxCropSize,
): CropResult = crop(maxResultSize) { UIImageSrc(uiImage) }

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [nsURL] will be used as a source.
 */
@OptIn(ExperimentalForeignApi::class)
suspend fun ImageCropper.crop(
    nsURL: NSURL,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize) { NSURLImageSrc(nsURL) }

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [phAsset] will be used as a source.
 */
suspend fun ImageCropper.crop(
    phAsset: PHAsset,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize) { PHAssetImageSrc(phAsset) }

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [localIdentifier] will be used as a source.
 */
suspend fun ImageCropper.cropPHAssetLocalIdentifier(
    localIdentifier: String,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize) { PHAssetImageSrc(localIdentifier) }

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [path] will be used as a source.
 */
@OptIn(ExperimentalForeignApi::class)
suspend fun ImageCropper.cropPath(
    path: String,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize) { NSURLImageSrc(path) }
