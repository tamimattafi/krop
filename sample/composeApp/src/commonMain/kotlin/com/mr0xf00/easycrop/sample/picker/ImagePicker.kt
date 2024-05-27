package com.mr0xf00.easycrop.sample.picker

import androidx.compose.runtime.Composable
import com.mr0xf00.easycrop.core.images.ImageSrc

interface ImagePicker {
    /** Pick an image with [mimetype] */
    fun pick(mimetype: String = "image/*")
}

@Composable
expect fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker