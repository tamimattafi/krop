package com.attafitamim.krop.filekit

import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.krop.core.images.ImageSrc
import io.github.vinceglb.filekit.PlatformFile

expect suspend fun PlatformFile.toImageSrc(): ImageSrc?

// Define supported formats
enum class ImageFormat {
    PNG, JPEG
}

/**
 * Encodes the ImageBitmap into a ByteArray using the specified format and quality.
 *
 * @param format The desired output format (PNG or JPEG). Defaults to JPEG.
 * @param quality The compression quality (0-100) when using JPEG format. Ignored for PNG. Defaults to 100.
 * @return ByteArray containing the encoded image data.
 * @throws Exception if encoding fails.
 */
expect suspend fun ImageBitmap.encodeToByteArray(
    format: ImageFormat = ImageFormat.JPEG,
    quality: Int = 100
): ByteArray
