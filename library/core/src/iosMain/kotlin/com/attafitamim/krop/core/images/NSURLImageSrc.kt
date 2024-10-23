package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.utils.getImageSize
import com.attafitamim.krop.core.utils.toImageBitmap
import com.attafitamim.krop.core.utils.toNSURL
import com.attafitamim.krop.core.utils.toUIImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

class NSURLImageSrc(
    private val nsURL: NSURL,
    override val size: IntSize
) : ImageSrc {

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = nsURL.toUIImage()
            ?.toImageBitmap(params)
            ?: return null

        return DecodeResult(params, bitmap)
    }

    companion object {

        @ExperimentalForeignApi
        operator fun invoke(nsURL: NSURL): NSURLImageSrc? {
            val size = nsURL.getImageSize() ?: return null
            return NSURLImageSrc(nsURL, size)
        }

        @ExperimentalForeignApi
        operator fun invoke(path: String): NSURLImageSrc? {
            val nsURL = path.toNSURL()
            return invoke(nsURL)
        }
    }
}