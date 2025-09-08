package com.attafitamim.krop.filekit

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Encodes the ImageBitmap into a ByteArray using the specified format and quality.
 *
 * @param format The desired output format (PNG or JPEG). Defaults to JPEG.
 * @param quality The compression quality (0-100) when using JPEG format. Ignored for PNG. Defaults to 100.
 * @return ByteArray containing the encoded image data.
 * @throws Exception if encoding fails.
 */
actual suspend fun ImageBitmap.encodeToByteArray(
    format: ImageFormat,
    quality: Int
): ByteArray = withContext(Dispatchers.Unconfined) {
    val bitmap = this@encodeToByteArray.asSkiaBitmap()
    val imageFormat = when (format) {
        ImageFormat.JPEG -> EncodedImageFormat.JPEG
        ImageFormat.PNG -> EncodedImageFormat.PNG
        ImageFormat.WEBP -> EncodedImageFormat.WEBP
    }
    Image
        .makeFromBitmap(bitmap)
        .encodeToData(imageFormat, quality)
        ?.bytes ?: ByteArray(0)
}
