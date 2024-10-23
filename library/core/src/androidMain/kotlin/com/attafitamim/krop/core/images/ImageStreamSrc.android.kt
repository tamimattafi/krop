package com.attafitamim.krop.core.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.exifinterface.media.ExifInterface
import com.attafitamim.krop.core.utils.validateSize
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEFAULT_SCALE = 1f
private const val DEFAULT_ROTATION_ANGLE = 90f
private const val VERTICAL_ROTATION_ANGLE = 180f

data class ImageStreamSrc(
    private val dataSource: ImageStream,
    override val size: IntSize
) : ImageSrc {

    private val allowRegion = AtomicBoolean(false)

    private suspend fun openRegion(params: DecodeParams): DecodeResult? {
        return dataSource.tryUse { stream ->
            regionDecoder(stream)!!.decodeRegion(params)
        }?.let { bmp ->
            DecodeResult(params, bmp.asImageBitmap())
        }
    }

    private suspend fun openFull(sampleSize: Int, orientation: Int): DecodeResult? {
        //BitmapFactory.decode supports more formats than BitmapRegionDecoder.
        return dataSource.tryUse { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeStream(stream, null, options)
                ?.ensureCorrectOrientation(orientation)
        }?.let { bmp ->
            DecodeResult(DecodeParams(sampleSize, size.toIntRect()), bmp.asImageBitmap())
        }
    }

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val orientation = dataSource.getOrientation()
        if (!isImageSizeFlipped(orientation) && allowRegion.get()) {
            val region = openRegion(params)
            if (region != null) return region
            else allowRegion.set(false)
        }

        return openFull(params.sampleSize, orientation)
    }

    companion object {
        suspend operator fun invoke(dataSource: ImageStream): ImageStreamSrc? {
            val size = dataSource.getImageSize() ?: return null
            return ImageStreamSrc(dataSource, size)
        }
    }
}

suspend fun <R> ImageStream.tryUse(op: suspend (InputStream) -> R): R? =
    withContext(Dispatchers.IO) {
        openStream()?.use { stream -> runCatching { op(stream) } }
    }?.onFailure {
        it.printStackTrace()
    }?.getOrNull()

fun regionDecoder(stream: InputStream): BitmapRegionDecoder? {
    @Suppress("DEPRECATION")
    return BitmapRegionDecoder.newInstance(stream, false)
}

fun BitmapRegionDecoder.decodeRegion(params: DecodeParams): Bitmap? {
    val rect = params.subset.toAndroidRect()
    val options = bitmapFactoryOptions(params.sampleSize)
    return decodeRegion(rect, options)
}

fun IntRect.toAndroidRect(): android.graphics.Rect {
    return android.graphics.Rect(left, top, right, bottom)
}

fun bitmapFactoryOptions(sampleSize: Int) = BitmapFactory.Options().apply {
    inMutable = false
    inSampleSize = sampleSize
}

suspend fun ImageStream.getOrientation(): Int {
    val exif = tryUse(::ExifInterface) ?: return ExifInterface.ORIENTATION_UNDEFINED
    return exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )
}

suspend fun ImageStream.getImageSize(): IntSize? = tryUse { stream ->
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeStream(stream, null, options)
    options
}?.let { options ->
    val orientation = getOrientation()
    val actualSize = if (isImageSizeFlipped(orientation)) {
        IntSize(options.outHeight, options.outWidth)
    } else {
        IntSize(options.outWidth, options.outHeight)
    }

    actualSize
}?.validateSize()

fun isImageSizeFlipped(orientation: Int): Boolean =
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_270,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE -> true
        else -> false
    }

fun Bitmap.ensureCorrectOrientation(orientation: Int): Bitmap {
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return this@ensureCorrectOrientation
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-DEFAULT_SCALE, DEFAULT_SCALE)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(VERTICAL_ROTATION_ANGLE)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(DEFAULT_SCALE, -DEFAULT_SCALE)

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                setRotate(DEFAULT_ROTATION_ANGLE)
                postScale(-DEFAULT_SCALE, DEFAULT_SCALE)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(DEFAULT_ROTATION_ANGLE)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                setRotate(-DEFAULT_ROTATION_ANGLE)
                postScale(-DEFAULT_SCALE, DEFAULT_SCALE)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-DEFAULT_ROTATION_ANGLE)
            else -> return this@ensureCorrectOrientation
        }
    }

    val rotatedBitmap = Bitmap.createBitmap(
        this,
        0,
        0,
        width,
        height,
        matrix,
        true
    )

    recycle()
    return rotatedBitmap
}