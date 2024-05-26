package com.mr0xf00.easycrop.core.images

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

fun interface ImageStream {
    fun openStream(): InputStream?
}

suspend fun Uri.toImageSrc(context: Context) = ImageStreamSrc(UriImageStream(this, context))
suspend fun File.toImageSrc() = ImageStreamSrc(FileImageStream(this))

data class FileImageStream(val file: File) : ImageStream {
    override fun openStream(): InputStream = file.inputStream()
}

data class UriImageStream(val uri: Uri, val context: Context) : ImageStream {
    override fun openStream(): InputStream? = context.contentResolver.openInputStream(uri)
}

