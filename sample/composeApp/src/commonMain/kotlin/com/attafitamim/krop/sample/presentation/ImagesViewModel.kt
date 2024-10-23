package com.attafitamim.krop.sample.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.imageCropper
import com.attafitamim.krop.core.images.ImageSrc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImagesViewModel : ViewModel() {
    val imageCropper = imageCropper()
    private val _selectedImage = MutableStateFlow<ImageBitmap?>(null)
    val selectedImage = _selectedImage.asStateFlow()
    private val _cropError = MutableStateFlow<CropError?>(null)
    val cropError = _cropError.asStateFlow()

    fun cropErrorShown() {
        _cropError.value = null
    }

    fun setSelectedImage(imageSrc: ImageSrc) {
        viewModelScope.launch {
            when(val result = imageCropper.crop(imageSrc = imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> _cropError.value = result
                is CropResult.Success -> {
                    _selectedImage.value = result.bitmap
                }
            }
        }
    }
}