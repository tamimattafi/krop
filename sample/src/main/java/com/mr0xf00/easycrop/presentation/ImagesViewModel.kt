package com.mr0xf00.easycrop.presentation

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mr0xf00.easycrop.core.crop.CropError
import com.mr0xf00.easycrop.core.crop.CropResult
import com.mr0xf00.easycrop.core.crop.ImageCropper
import com.mr0xf00.easycrop.core.crop.cropSrc
import com.mr0xf00.easycrop.core.images.ImageSrc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImagesViewModel(private val app: Application) : AndroidViewModel(app) {
    val imageCropper = ImageCropper()
    private val _selectedImage = MutableStateFlow<ImageBitmap?>(null)
    val selectedImage = _selectedImage.asStateFlow()
    private val _cropError = MutableStateFlow<CropError?>(null)
    val cropError = _cropError.asStateFlow()

    fun cropErrorShown() {
        _cropError.value = null
    }

    fun setSelectedImage(imageSrc: ImageSrc) {
        viewModelScope.launch {
            when(val result = imageCropper.cropSrc(imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> _cropError.value = result
                is CropResult.Success -> {
                    _selectedImage.value = result.bitmap
                }
            }
        }
    }
}