package com.mr0xf00.easycrop.sample.picker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.mr0xf00.easycrop.core.images.ImageSrc
import com.mr0xf00.easycrop.core.images.toImageSrc
import com.mr0xf00.easycrop.sample.picker.ImagePicker
import kotlinx.coroutines.launch

/** Creates and remembers a instance of [ImagePicker] that launches
 * [ActivityResultContracts.GetContent] and calls [onImage] when the result is available */
@Composable
actual fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit
): ImagePicker {
    val context = LocalContext.current
    val contract = remember { ActivityResultContracts.GetContent() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = { uri ->
            coroutineScope.launch {
                val imageSrc = uri?.toImageSrc(context) ?: return@launch
                onImage(imageSrc)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = launcher.launch(mimetype)
        }
    }
}