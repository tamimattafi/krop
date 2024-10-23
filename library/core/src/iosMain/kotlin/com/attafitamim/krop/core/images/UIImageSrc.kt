package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.utils.ensureCorrectOrientation
import com.attafitamim.krop.core.utils.getSize
import com.attafitamim.krop.core.utils.toImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage

class UIImageSrc(
    private val image: UIImage,
    override val size: IntSize
) : ImageSrc {

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = image.toImageBitmap(params) ?: return null
        return DecodeResult(params, bitmap)
    }

    companion object {
        @ExperimentalForeignApi
        operator fun invoke(sourceImage: UIImage): UIImageSrc? {
            val image = sourceImage.ensureCorrectOrientation()
            val size = image.getSize() ?: return null

            return UIImageSrc(image, size)
        }
    }
}
