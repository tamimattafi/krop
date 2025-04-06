package com.attafitamim.krop.sample.ui

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery

actual suspend fun saveImage(bytes: ByteArray, fileName: String) {
    // Save to gallery
    FileKit.saveImageToGallery(bytes, "cropped_image.jpg")

    // Alternatively, save to app-specific directory
    // val file = FileKit.filesDir / "cropped_image.jpg"
    // file.write(bytes)
}
