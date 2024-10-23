package com.attafitamim.krop.core.images

import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.utils.getNSURL
import com.attafitamim.krop.core.utils.getSize
import com.attafitamim.krop.core.utils.loadPHAsset
import com.attafitamim.krop.core.utils.toImageBitmap
import com.attafitamim.krop.core.utils.toUIImage
import platform.Photos.PHAsset

class PHAssetImageSrc(
    private val phAsset: PHAsset,
    override val size: IntSize
) : ImageSrc {

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val bitmap = phAsset.getNSURL()
            ?.toUIImage()
            ?.toImageBitmap(params)
            ?: return null

        return DecodeResult(params, bitmap)
    }

    companion object {
        operator fun invoke(phAsset: PHAsset): PHAssetImageSrc? {
            val size = phAsset.getSize() ?: return null
            return PHAssetImageSrc(phAsset, size)
        }

        operator fun invoke(localIdentifier: String): PHAssetImageSrc? {
            val phAsset = loadPHAsset(localIdentifier) ?: return null
            return invoke(phAsset)
        }
    }
}

