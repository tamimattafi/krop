package com.attafitamim.krop.core.images

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import com.attafitamim.krop.core.utils.validateSize
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.bmp.BmpHeaderDirectory
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.file.FileTypeDirectory
import com.drew.metadata.gif.GifImageDirectory
import com.drew.metadata.ico.IcoDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.drew.metadata.png.PngDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import javax.imageio.ImageReader


data class ImageStreamSrc(
    private val dataSource: ImageStream,
    override val size: IntSize
) : ImageSrc {
    private val allowRegion = AtomicBoolean(false)

    /**
     * Opens image region with specified sample size and orientation
     * @param params DecodeParams containing sample size and region to decode
     * @return DecodeResult containing the processed image
     */
    private suspend fun openRegion(params: DecodeParams): DecodeResult? {
        return dataSource.tryUse { stream ->
            getImageReader(stream)?.decodeRegion(params)
        }?.let { bufferedImage ->
            DecodeResult(params, bufferedImage.toImageBitmap())
        }
    }

    /**
     * Opens full image with specified sample size and orientation
     * @param sampleSize downscaling factor
     * @param orientation EXIF orientation value
     * @return DecodeResult containing the processed image
     */
    private suspend fun openFull(sampleSize: Int, orientation: Int): DecodeResult? {
        return dataSource.tryUse { stream ->
            getImageReader(stream)?.decodeFull(sampleSize)?.ensureCorrectOrientation(orientation)
        }?.let { bmp ->
            DecodeResult(DecodeParams(sampleSize, size.toIntRect()), bmp.toImageBitmap())
        }
    }

    override suspend fun open(params: DecodeParams): DecodeResult? {
        val orientation = dataSource.getOrientation()
        if (!isImageSizeFlipped(orientation) && allowRegion.get()) {
            val region = openRegion(params)
            if (region != null) return region
            else allowRegion.set(false)
        }

        return openFull(params.sampleSize, orientation)
    }

    companion object {
        suspend operator fun invoke(dataSource: ImageStream): ImageSrc? {
            val size = dataSource.getImageSize()?.validateSize() ?: return null
            return ImageStreamSrc(dataSource, size)
        }
    }
}

private suspend fun <R> ImageStream.tryUse(op: suspend (InputStream) -> R): R? {
    return withContext(Dispatchers.IO) {
        try {
            openStream()?.use { stream -> op(stream) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun getImageReader(stream: InputStream): ImageReader? {
    val imageInputStream = ImageIO.createImageInputStream(stream)
    val readers = ImageIO.getImageReaders(imageInputStream)
    return if (readers.hasNext()) readers.next().apply { input = imageInputStream } else null
}

private fun ImageReader.decodeRegion(params: DecodeParams): BufferedImage? {
    val rect = params.subset
    return try {
        val param = defaultReadParam
        param.sourceRegion = java.awt.Rectangle(
            rect.left, rect.top, rect.width, rect.height
        )
        param.setSourceSubsampling(params.sampleSize, params.sampleSize, 0, 0)
        read(0, param)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun ImageReader.decodeFull(sampleSize: Int) = try {
    val param = defaultReadParam
    param.setSourceSubsampling(sampleSize, sampleSize, 0, 0)
    read(0, param)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

suspend fun ImageStream.getOrientation(): Int {
    return tryUse { stream ->
        val metadata = ImageMetadataReader.readMetadata(stream)
        metadata.getOrientation()
    } ?: 1 // normal orientation by default
}

/**
 * Get image size. The method will try to read image metadata to get the image size first, if failed, it will
 * try to read image data to get the result.
 *
 * @return image size or null if failed to get
 */
private suspend fun ImageStream.getImageSize(): IntSize? = tryUse { stream ->
    try {
        val metadata = ImageMetadataReader.readMetadata(stream)
        // read MIME type from metadata
        val fileTypeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory::class.java)
        val mimeType = fileTypeDirectory?.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)

        var width: Int? = null
        var height: Int? = null

        when (mimeType) {
            "image/jpeg" -> {
                val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
                width = jpegDirectory?.getInt(JpegDirectory.TAG_IMAGE_WIDTH)
                height = jpegDirectory?.getInt(JpegDirectory.TAG_IMAGE_HEIGHT)
            }
            "image/png" -> {
                val pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory::class.java)
                width = pngDirectory?.getInt(PngDirectory.TAG_IMAGE_WIDTH)
                height = pngDirectory?.getInt(PngDirectory.TAG_IMAGE_HEIGHT)
            }
            "image/gif" -> {
                val gifDirectory = metadata.getFirstDirectoryOfType(GifImageDirectory::class.java)
                width = gifDirectory?.getInt(GifImageDirectory.TAG_WIDTH)
                height = gifDirectory?.getInt(GifImageDirectory.TAG_HEIGHT)
            }
            "image/bmp" -> {
                val bmpDirectory = metadata.getFirstDirectoryOfType(BmpHeaderDirectory::class.java)
                width = bmpDirectory?.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH)
                height = bmpDirectory?.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT)
            }
            "image/x-icon" -> {
                val icoDirectory = metadata.getFirstDirectoryOfType(IcoDirectory::class.java)
                width = icoDirectory?.getInt(IcoDirectory.TAG_IMAGE_WIDTH)
                height = icoDirectory?.getInt(IcoDirectory.TAG_IMAGE_HEIGHT)
            }
            else -> { }
        }

        if (width != null && height != null) {
            val orientation = metadata.getOrientation()
            if (isImageSizeFlipped(orientation)) {
                IntSize(height, width)
            } else {
                IntSize(width, height)
            }
        } else {
            // Fallback to ImageIO if EXIF data is not available
            stream.reset()
            val reader = getImageReader(stream) ?: return@tryUse null

            try {
                width = reader.getWidth(0)
                height = reader.getHeight(0)
                val orientation = metadata.getOrientation()

                if (isImageSizeFlipped(orientation)) {
                    IntSize(height, width)
                } else {
                    IntSize(width, height)
                }
            } finally {
                reader.dispose()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun BufferedImage.toImageBitmap(): ImageBitmap {
    // convert BufferedImage to byte array
    val baos = java.io.ByteArrayOutputStream()
    ImageIO.write(this, "png", baos)
    val bytes = baos.toByteArray()

    // create ImageBitmap from byte array by Skia
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

private fun BufferedImage.ensureCorrectOrientation(orientation: Int): BufferedImage {
    if (orientation == ImageOrientation.NORMAL) return this

    val transform = AffineTransform()
    val width = width.toDouble()
    val height = height.toDouble()

    when (orientation) {
        ImageOrientation.FLIP_HORIZONTAL -> {
            transform.translate(width, 0.0)
            transform.scale(-1.0, 1.0)
        }
        ImageOrientation.ROTATE_180 -> {
            transform.translate(width, height)
            transform.rotate(Math.PI)
        }
        ImageOrientation.FLIP_VERTICAL -> {
            transform.translate(0.0, height)
            transform.scale(1.0, -1.0)
        }
        ImageOrientation.ROTATE_90_CW_FLIP_HORIZONTAL -> {
            transform.rotate(0.5 * Math.PI)
            transform.scale(-1.0, 1.0)
        }
        ImageOrientation.ROTATE_90_CW -> {
            transform.translate(height, 0.0)
            transform.rotate(0.5 * Math.PI)
        }
        ImageOrientation.ROTATE_90_CCW_FLIP_HORIZONTAL -> {
            transform.scale(-1.0, 1.0)
            transform.translate(-height, 0.0)
            transform.translate(0.0, width)
            transform.rotate(-0.5 * Math.PI)
        }
        ImageOrientation.ROTATE_90_CCW -> {
            transform.translate(0.0, width)
            transform.rotate(-0.5 * Math.PI)
        }
    }

    val destinationType = if (type == BufferedImage.TYPE_CUSTOM) {
        BufferedImage.TYPE_INT_ARGB
    } else {
        type
    }

    // Calculate destination dimensions based on orientation
    val isImageFlipped = isImageSizeFlipped(orientation)
    val (destinationWidth, destinationHeight) = if (isImageFlipped) {
        height.toInt() to width.toInt()
    } else {
        width.toInt() to height.toInt()
    }

    val destinationImage = BufferedImage(destinationWidth, destinationHeight, destinationType)
    val graphics = destinationImage.createGraphics()

    try {
        graphics.transform(transform)
        graphics.drawImage(this, 0, 0, null)
        return destinationImage
    } finally {
        graphics.dispose()
    }
}

/**
 * Checks if the image dimensions should be flipped based on EXIF orientation
 * @param orientation EXIF orientation value
 * @return true if width and height should be swapped
 */
private fun isImageSizeFlipped(orientation: Int): Boolean {
    return when (orientation) {
        ImageOrientation.ROTATE_90_CW, ImageOrientation.ROTATE_90_CW_FLIP_HORIZONTAL,
        ImageOrientation.ROTATE_90_CCW, ImageOrientation.ROTATE_90_CCW_FLIP_HORIZONTAL -> true
        else -> false
    }
}

private fun Metadata.getOrientation(): Int {
    val directory = getFirstDirectoryOfType(ExifIFD0Directory::class.java)
    return directory?.getInteger(ExifIFD0Directory.TAG_ORIENTATION) ?: ImageOrientation.NORMAL
}