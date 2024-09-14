package com.attafitamim.krop.sample.picker

import androidx.compose.runtime.Composable
import com.attafitamim.krop.core.images.ImageSrc

interface ImagePicker {
    /** Pick an image with [mimetype] */
    fun pick(mimetype: String = "image/*")
}

@Composable
expect fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker