package com.attafitamim.krop.filekit

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.toImageSrc
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual suspend fun PlatformFile.toImageSrc(): ImageSrc? =
    this.androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.FileWrapper -> androidFile.file.toImageSrc()
            is AndroidFile.UriWrapper -> androidFile.uri.toImageSrc(FileKit.context)
        }
    }

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
): ByteArray = withContext(Dispatchers.IO) {
    val bitmap = this@encodeToByteArray.asAndroidBitmap()
    val compressFormat = when (format) {
        ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageFormat.PNG -> Bitmap.CompressFormat.PNG
    }
    ByteArrayOutputStream().use { bytes ->
        bitmap.compress(compressFormat, quality, bytes)
        bytes.toByteArray()
    }
}
