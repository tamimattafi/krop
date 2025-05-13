package com.attafitamim.krop.sample.ui

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write

actual suspend fun saveImage(bytes: ByteArray, fileName: String, extension: String) {
    val file = FileKit.openFileSaver(
        suggestedName = fileName,
        extension = extension
    )

    file?.write(bytes)
}
