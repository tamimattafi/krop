@file:OptIn(ExperimentalForeignApi::class)

package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import com.attafitamim.krop.core.utils.DEFAULT_BITMAP_COMPRESSION_QUALITY
import com.attafitamim.krop.core.utils.ensureCorrectOrientation
import com.attafitamim.krop.core.utils.getSize
import com.attafitamim.krop.core.utils.toImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage

class UIImageSrc(
    private val image: UIImage,
    override val size: IntSize,
    private val quality: Double = DEFAULT_BITMAP_COMPRESSION_QUALITY,
) : ImageSrc {

    private val resultParams = DecodeParams(1, size.toIntRect())

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = image.toImageBitmap(quality) ?: return null
        return DecodeResult(resultParams, bitmap)
    }

    companion object {
        operator fun invoke(
            sourceImage: UIImage,
            quality: Double = DEFAULT_BITMAP_COMPRESSION_QUALITY
        ): ImageSrc? {
            val image = sourceImage.ensureCorrectOrientation()
            val size = image.getSize() ?: return null

            return UIImageSrc(
                image = image,
                size = size,
                quality = quality
            )
        }
    }
}