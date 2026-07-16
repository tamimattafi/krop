package com.attafitamim.krop.core.crop

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile

/** Union type denoting the possible results after a crop operation is done */
sealed interface CropResult {
    data class Success(val bitmap: ImageBitmap) : CropResult

    /** The user has cancelled the operation or another session was started. */
    data object Cancelled : CropResult
}

enum class CropError : CropResult {
    /** The supplied image is invalid, not supported by the codec
     * or you don't have the required permissions to read it */
    LoadingError,
    /** The result could not be saved. Try reducing the maxSize supplied to [imageCropper.crop] */
    SavingError
}

/**
 * The geometry of an accepted crop — enough to re-apply the crop to the
 * original image later, without storing a rendered bitmap. This is the building
 * block for **non-destructive cropping**: keep the original image and persist a
 * [CropRegion], then re-render (or re-open the cropper seeded with it) on demand.
 */
data class CropRegion(
    /** Crop rectangle, in the source image's pixel coordinates. */
    val region: Rect,
    /** Rotation / flip applied to the image ([ImgTransform.Identity] when none). */
    val transform: ImgTransform,
    /** Source image dimensions, so [region] can be normalised for storage. */
    val imageSize: IntSize,
)

/** Union type denoting the possible results of an [ImageCropper.cropRegion] session. */
sealed interface CropRegionResult {
    data class Success(val cropRegion: CropRegion) : CropRegionResult

    /** The user has cancelled the operation or another session was started. */
    data object Cancelled : CropRegionResult

    /** The supplied image is invalid, unsupported, or could not be read. */
    data object LoadingError : CropRegionResult
}

enum class CropperLoading {
    /** The image is being prepared. */
    PreparingImage,

    /** The user has accepted the cropped image and the result is being saved. */
    SavingResult,
}

val DefaultMaxCropSize = IntSize(3000, 3000)

/**
 * State holder for the image cropper.
 * Allows starting new crop sessions as well as getting the state of the pending crop.
 */
@Stable
interface ImageCropper {
    /** The pending crop state, if any */
    val cropState: CropState?

    val loadingStatus: CropperLoading?

    /**
     * Initiates a new crop session, cancelling the current one, if any.
     * Suspends until a result is available (cancellation, error, success) and returns it.
     * The resulting image will be scaled down to fit [maxResultSize] (if provided).
     * [createSrc] will be used to construct an [ImageSrc] instance.
     */
    suspend fun crop(
        maxResultSize: IntSize? = DefaultMaxCropSize,
        createSrc: suspend () -> ImageSrc?
    ): CropResult

    /**
     * Initiates a new crop session like [crop], but returns the crop **geometry**
     * ([CropRegion]) instead of a rendered bitmap. No result image is produced.
     *
     * Use this for non-destructive cropping: keep the original image and persist
     * the returned [CropRegion] to re-apply the crop on demand. Suspends until the
     * session ends (accept / cancel / load error). [createSrc] builds the
     * [ImageSrc] to crop.
     *
     * Pass [initial] to seed the session at a previously-saved [CropRegion], so a
     * "re-crop" opens exactly where the user left off instead of the full image.
     */
    suspend fun cropRegion(
        initial: CropRegion? = null,
        createSrc: suspend () -> ImageSrc?
    ): CropRegionResult
}

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [bmp] will be used as a source.
 */
suspend fun ImageCropper.crop(
    bmp: ImageBitmap,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize = maxResultSize) {
    ImageBitmapSrc(bmp)
}

suspend fun ImageCropper.crop(
    imageSrc: ImageSrc?,
    maxResultSize: IntSize? = DefaultMaxCropSize
): CropResult = crop(maxResultSize = maxResultSize) {
    imageSrc
}

/**
 * [cropRegion] overload sourcing the session from an in-memory [bmp]. Returns the
 * crop geometry for non-destructive use. Pass [initial] to seed a re-crop.
 */
suspend fun ImageCropper.cropRegion(
    bmp: ImageBitmap,
    initial: CropRegion? = null
): CropRegionResult = cropRegion(initial) {
    ImageBitmapSrc(bmp)
}

suspend fun ImageCropper.cropRegion(
    imageSrc: ImageSrc?,
    initial: CropRegion? = null
): CropRegionResult = cropRegion(initial) {
    imageSrc
}

@Composable
fun rememberImageCropper() : ImageCropper {
    return remember { imageCropper() }
}

/**
 * Creates an [imageCropper] instance.
 */
fun imageCropper(): ImageCropper = object : ImageCropper {
    override var cropState: CropState? by mutableStateOf(null)
    private val cropStateFlow = snapshotFlow { cropState }
    override var loadingStatus: CropperLoading? by mutableStateOf(null)

    override suspend fun crop(
        maxResultSize: IntSize?,
        createSrc: suspend () -> ImageSrc?
    ): CropResult {
        val newCrop = runSession(initial = null, createSrc = createSrc)
            ?: return CropError.LoadingError
        if (!newCrop.accepted) return CropResult.Cancelled
        return withLoading(CropperLoading.SavingResult) {
            val result = newCrop.createResult(maxResultSize)
            if (result == null) CropError.SavingError
            else CropResult.Success(result)
        }
    }

    override suspend fun cropRegion(
        initial: CropRegion?,
        createSrc: suspend () -> ImageSrc?
    ): CropRegionResult {
        val newCrop = runSession(initial = initial, createSrc = createSrc)
            ?: return CropRegionResult.LoadingError
        if (!newCrop.accepted) return CropRegionResult.Cancelled
        return CropRegionResult.Success(
            CropRegion(
                region = newCrop.region,
                transform = newCrop.transform,
                imageSize = newCrop.src.size,
            )
        )
    }

    /**
     * Prepares the image, starts a crop session (optionally seeded at [initial]),
     * and suspends until it ends. Returns the (possibly un-accepted) [CropState],
     * or null if the image failed to load. Shared by [crop] and [cropRegion].
     */
    private suspend fun runSession(
        initial: CropRegion?,
        createSrc: suspend () -> ImageSrc?
    ): CropState? {
        cropState = null
        val src = withLoading(CropperLoading.PreparingImage) { createSrc() } ?: return null
        val newCrop = cropState(src, initial) { cropState = null }
        cropState = newCrop
        cropStateFlow.takeWhile { it === newCrop }.collect()
        return newCrop
    }

    inline fun <R> withLoading(status: CropperLoading, op: () -> R): R {
        return try {
            loadingStatus = status
            op()
        } finally {
            loadingStatus = null
        }
    }
}
