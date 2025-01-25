package com.attafitamim.krop.sample.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.toImageSrc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberImagePicker(onImage: (uri: ImageSrc) -> Unit): ImagePicker {
    val coroutineScope = rememberCoroutineScope()
    val currentOnImage by rememberUpdatedState(onImage)
    return remember {
        DesktopImagePicker { file ->
            coroutineScope.launch {
                runCatching { file.toImageSrc() }.getOrNull()?.let { src ->
                    currentOnImage(src)
                }
            }
        }
    }
}

class DesktopImagePicker(
    private val onFile: suspend (File) -> Unit
) : ImagePicker {
    private val userHomePath by lazy { System.getProperty("user.home") }
    private val fileDialog = FileDialog(null as Frame?, "Select an image", FileDialog.LOAD)

    override fun pick(mimetype: String) {
        fileDialog.apply {
            if (directory == null) {
                directory = userHomePath
            }
            setFilenameFilter { _, name ->
                val lowerCaseName = name.lowercase()
                val extensions = mimetype.mimeToExtensions()
                extensions.any {
                    lowerCaseName.endsWith(it)
                }
            }
            isVisible = true

            file?.let { fileName ->
                directory?.let { dir ->
                    val selectedFile = File(dir, fileName)
                    CoroutineScope(Dispatchers.Main).launch {
                        onFile(selectedFile)
                    }
                }
            }
        }
    }
}

private fun String.mimeToExtensions(): Array<String> {
    return when (this) {
        "image/jpeg" -> arrayOf("jpg", "jpeg", "jfif", "pjpeg", "pjp")
        "image/png" -> arrayOf("png")
        "image/gif" -> arrayOf("gif")
        "image/bmp" -> arrayOf("bmp")
        "image/ico" -> arrayOf("ico", "cur")
        "image/*" -> arrayOf("jpg", "jpeg", "jfif", "pjpeg", "pjp", "png", "gif", "bmp", "ico", "cur")
        else -> arrayOf()
    }
}