package com.mr0xf00.easycrop.ui
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.mr0xf00.easycrop.*
import com.mr0xf00.easycrop.core.crop.CropError
import com.mr0xf00.easycrop.core.crop.CropResult
import com.mr0xf00.easycrop.core.crop.cropSrc
import com.mr0xf00.easycrop.core.crop.rememberImageCropper
import kotlinx.coroutines.launch

@Composable
fun SimpleDemo(modifier: Modifier = Modifier) {
    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<CropError?>(null) }
    val imagePicker = rememberImagePicker(onImage = { imageSrc ->
        scope.launch {
            when (val result = imageCropper.cropSrc(imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> error = result
                is CropResult.Success -> {
                    selectedImage = result.bitmap
                }
            }
        }
    })
    DemoContent(
        cropState = imageCropper.cropState,
        loadingStatus = imageCropper.loadingStatus,
        selectedImage = selectedImage,
        onPick = { imagePicker.pick() },
        modifier = modifier
    )
    error?.let { CropErrorDialog(it, onDismiss = { error = null }) }
}

