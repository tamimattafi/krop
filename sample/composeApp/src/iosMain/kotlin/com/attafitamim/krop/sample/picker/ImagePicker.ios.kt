package com.attafitamim.krop.sample.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.attafitamim.krop.core.images.ImageSrc
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import org.jetbrains.skia.Image
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController

private const val COMPRESSION_QUALITY = 0.99

@Composable
actual fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker {
    val imagePicker = remember {
        UIImagePickerController()
    }

    val galleryDelegate = remember {
        ImagePickerDelegate(onImage)
    }

    return remember {
        IosImagePicker(imagePicker, galleryDelegate)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toByteArray(): ByteArray {
    val imageData = UIImageJPEGRepresentation(this, COMPRESSION_QUALITY)
        ?: throw IllegalArgumentException("image data is null")

    val bytes = imageData.bytes
        ?: throw IllegalArgumentException("image bytes is null")

    val length = imageData.length

    val data: CPointer<ByteVar> = bytes.reinterpret()
    return ByteArray(length.toInt()) { index -> data[index] }
}

fun UIImage.toImageBitmap(): ImageBitmap {
    val byteArray = toByteArray()
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}
