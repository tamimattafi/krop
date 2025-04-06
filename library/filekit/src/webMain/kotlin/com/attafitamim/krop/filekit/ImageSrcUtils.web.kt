package com.attafitamim.krop.filekit

import androidx.compose.ui.graphics.toComposeImageBitmap
import com.attafitamim.krop.core.images.ImageBitmapSrc
import com.attafitamim.krop.core.images.ImageSrc
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import org.jetbrains.skia.Image

actual suspend fun PlatformFile.toImageSrc(): ImageSrc? {
    val bytes = readBytes()
    val image = Image.makeFromEncoded(bytes)
    val bitmap = image.toComposeImageBitmap()
    return ImageBitmapSrc(bitmap)
}
