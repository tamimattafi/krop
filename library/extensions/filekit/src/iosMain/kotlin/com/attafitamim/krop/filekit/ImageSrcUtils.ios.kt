package com.attafitamim.krop.filekit

import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.UIImageSrc
import com.attafitamim.krop.core.utils.toUIImage
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun PlatformFile.toImageSrc(): ImageSrc? {
    val uiImage = nsUrl.toUIImage() ?: return null
    return UIImageSrc(uiImage)
}
