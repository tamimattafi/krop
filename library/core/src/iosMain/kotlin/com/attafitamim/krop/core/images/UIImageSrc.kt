@file:OptIn(ExperimentalForeignApi::class)

package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import com.attafitamim.krop.core.utils.ensureCorrectOrientation
import com.attafitamim.krop.core.utils.toImageBitmap
import kotlin.math.roundToInt
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIImage

class UIImageSrc(
    private val image: UIImage,
    override val size: IntSize,
    private val quality: Double = 1.0,
) : ImageSrc {

    private val resultParams = DecodeParams(1, size.toIntRect())

    companion object {
        operator fun invoke(sourceImage: UIImage): UIImageSrc {
            val image = sourceImage.ensureCorrectOrientation()
            val size = image.size.useContents {
                IntSize(width.roundToInt(), height.roundToInt())
            }

            return UIImageSrc(image, size)
        }
    }

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = image.toImageBitmap(quality) ?: return null
        return DecodeResult(resultParams, bitmap)
    }
}