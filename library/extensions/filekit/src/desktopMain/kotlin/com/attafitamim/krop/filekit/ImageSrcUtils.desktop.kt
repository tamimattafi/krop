package com.attafitamim.krop.filekit

import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.toImageSrc
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun PlatformFile.toImageSrc(): ImageSrc? {
    return file.toImageSrc()
}
