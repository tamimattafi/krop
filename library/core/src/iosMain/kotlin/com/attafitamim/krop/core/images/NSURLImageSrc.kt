package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.utils.DEFAULT_BITMAP_COMPRESSION_QUALITY
import com.attafitamim.krop.core.utils.getImageSize
import com.attafitamim.krop.core.utils.toImageBitmap
import com.attafitamim.krop.core.utils.toNSURL
import com.attafitamim.krop.core.utils.toUIImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

class NSURLImageSrc(
    private val nsURL: NSURL,
    override val size: IntSize,
    private val quality: Double = DEFAULT_BITMAP_COMPRESSION_QUALITY,
) : ImageSrc {

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = nsURL.toUIImage()
            ?.toImageBitmap(quality)
            ?: return null

        return DecodeResult(params, bitmap)
    }

    companion object {

        @ExperimentalForeignApi
        operator fun invoke(
            nsURL: NSURL,
            quality: Double = DEFAULT_BITMAP_COMPRESSION_QUALITY,
        ): ImageSrc? {
            val size = nsURL.getImageSize() ?: return null
            return NSURLImageSrc(
                nsURL = nsURL,
                size = size,
                quality = quality
            )
        }

        @ExperimentalForeignApi
        operator fun invoke(
            path: String,
            quality: Double = DEFAULT_BITMAP_COMPRESSION_QUALITY,
        ): ImageSrc? {
            val nsURL = path.toNSURL()
            return invoke(
                nsURL = nsURL,
                quality = quality
            )
        }
    }
}