package com.mr0xf00.easycrop.core.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

data class ImageStreamSrc(
    private val dataSource: ImageStream,
    override val size: IntSize
) : ImageSrc {

    private val allowRegion = AtomicBoolean(true)

    private suspend fun openRegion(params: DecodeParams): DecodeResult? {
        return dataSource.tryUse { stream ->
            regionDecoder(stream)!!.decodeRegion(params)
        }?.let { bmp ->
            DecodeResult(params, bmp.asImageBitmap())
        }
    }

    private suspend fun openFull(sampleSize: Int): DecodeResult? {
        //BitmapFactory.decode supports more formats than BitmapRegionDecoder.
        return dataSource.tryUse { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeStream(stream, null, options)
        }?.let { bmp ->
            DecodeResult(DecodeParams(sampleSize, size.toIntRect()), bmp.asImageBitmap())
        }
    }

    override suspend fun open(params: DecodeParams): DecodeResult? {
        if (allowRegion.get()) {
            val region = openRegion(params)
            if (region != null) return region
            else allowRegion.set(false)
        }
        openFull(params.sampleSize)?.let { return it }
        return null
    }

    companion object {
        private suspend fun <R> ImageStream.tryUse(op: (InputStream) -> R): R? {
            return withContext(Dispatchers.IO) {
                openStream()?.use { stream -> runCatching { op(stream) } }
            }?.onFailure {
                it.printStackTrace()
            }?.getOrNull()
        }

        suspend operator fun invoke(dataSource: ImageStream): ImageStreamSrc? {
            val size = dataSource.tryUse { it.getImageSize() }
                ?.takeIf { it.width > 0 && it.height > 0 }
                ?: return null
            return ImageStreamSrc(dataSource, size)
        }
    }
}

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

fun InputStream.getImageSize(): IntSize {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeStream(this, null, options)
    return IntSize(options.outWidth, options.outHeight)
}