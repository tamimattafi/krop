package com.attafitamim.krop.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CircleCropShape
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.CropperLoading
import com.attafitamim.krop.core.crop.RectCropShape
import com.attafitamim.krop.core.crop.StarCropShape
import com.attafitamim.krop.core.crop.TriangleCropShape
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.sample.ui.theme.KropTheme
import com.attafitamim.krop.ui.ImageCropperDialog

@Composable
fun DemoContent(
    cropState: CropState?,
    loadingStatus: CropperLoading?,
    selectedImage: ImageBitmap?,
    onPick: () -> Unit,
    onSave: (ImageBitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cropState != null) {
        KropTheme(darkTheme = true) {
            ImageCropperDialog(
                state = cropState,
                style = cropperStyle(
                    shapes = listOf(RectCropShape, CircleCropShape, TriangleCropShape, StarCropShape),
                    aspects = listOf(AspectRatio(16, 9), AspectRatio(1, 1)),
                )
            )
        }
    }
    if (cropState == null && loadingStatus != null) {
        LoadingDialog(status = loadingStatus)
    }
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedImage != null) Image(
            bitmap = selectedImage, contentDescription = null,
            modifier = Modifier.weight(1f)
        ) else Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            Text("No image selected !")
        }
        if (selectedImage != null) {
            Button(onClick = { onSave(selectedImage) }) {
                Text("Save cropped image")
            }
        }
        Button(onClick = onPick) { Text("Choose Image") }
    }
}
