package com.attafitamim.krop.core.images

import java.io.File
import java.io.IOException
import java.io.InputStream

interface ImageStream {
    @Throws(IOException::class, NullPointerException::class)
    suspend fun openStream(): InputStream?
}

class FileImageStream(private val file: File) : ImageStream {
    override suspend fun openStream(): InputStream = file.inputStream()
}

@Throws(IOException::class, NullPointerException::class)
suspend fun File.toImageSrc() = DesktopImageStreamSrc(FileImageStream(this))
