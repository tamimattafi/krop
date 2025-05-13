package com.attafitamim.krop.sample.ui

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download

actual suspend fun saveImage(bytes: ByteArray, fileName: String, extension: String) {
    val fullName = "$fileName.$extension"
    FileKit.download(bytes, fullName)
}
