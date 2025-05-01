package com.attafitamim.krop.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.encodeToByteArray
import com.attafitamim.krop.filekit.toImageSrc
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch

@Composable
fun SimpleDemo(modifier: Modifier = Modifier) {
    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<CropError?>(null) }
    val imagePicker = rememberFilePickerLauncher(type = FileKitType.Image) { image ->
        image?.let {
            scope.launch {
                val imageSrc = image.toImageSrc()
                when (val result = imageCropper.crop(imageSrc)) {
                    CropResult.Cancelled -> {}
                    is CropError -> error = result
                    is CropResult.Success -> selectedImage = result.bitmap
                }
            }
        }
    }
    DemoContent(
        cropState = imageCropper.cropState,
        loadingStatus = imageCropper.loadingStatus,
        selectedImage = selectedImage,
        onPick = { imagePicker.launch() },
        onSave = { bitmap ->
            scope.launch {
                // Convert ImageBitmap to ByteArray
                val bytes = bitmap.encodeToByteArray()

                // Save the cropped image
                saveImage(
                    bytes = bytes,
                    fileName = "cropped_image",
                    extension = "jpg",
                )
            }
        },
        modifier = modifier
    )
    error?.let { CropErrorDialog(it, onDismiss = { error = null }) }
}

/**
 * On web platform (WASM), it downloads the image via the browser.
 * On iOS and Android, it saves the image to the gallery.
 * On desktop, it saves the image to the user pictures folders (ex. ~/Pictures).
 */
expect suspend fun saveImage(
    bytes: ByteArray,
    fileName: String,
    extension: String,
)
