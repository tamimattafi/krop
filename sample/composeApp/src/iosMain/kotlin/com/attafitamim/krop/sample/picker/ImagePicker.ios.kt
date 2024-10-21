package com.attafitamim.krop.sample.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.attafitamim.krop.core.images.ImageSrc
import platform.UIKit.UIImagePickerController

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
