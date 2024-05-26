package com.mr0xf00.easycrop.sample.ui
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.mr0xf00.easycrop.sample.presentation.ImagesViewModel
import com.mr0xf00.easycrop.ui.rememberImagePicker

@Composable
fun ViewModelDemo(viewModel: ImagesViewModel, modifier: Modifier = Modifier) {
    val imagePicker = rememberImagePicker(onImage = { uri -> viewModel.setSelectedImage(uri) })
    DemoContent(
        cropState = viewModel.imageCropper.cropState,
        loadingStatus = viewModel.imageCropper.loadingStatus,
        selectedImage = viewModel.selectedImage.collectAsState().value,
        onPick = { imagePicker.pick() },
        modifier = modifier
    )
    viewModel.cropError.collectAsState().value?.let { error ->
        CropErrorDialog(error, onDismiss = { viewModel.cropErrorShown() })
    }
}