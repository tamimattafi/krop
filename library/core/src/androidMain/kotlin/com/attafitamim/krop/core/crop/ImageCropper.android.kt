package com.attafitamim.krop.core.crop

import android.content.Context
import android.net.Uri
import androidx.compose.ui.unit.IntSize
import com.attafitamim.krop.core.images.ImageStream
import com.attafitamim.krop.core.images.ImageStreamSrc
import com.attafitamim.krop.core.images.UriImageStream
import com.attafitamim.krop.core.images.toImageSrc
import com.attafitamim.krop.core.images.tryUse
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val CropCacheDir = "krop_cache"

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [file] will be used as a source.
 */
suspend fun ImageCropper.crop(
    file: File,
    maxResultSize: IntSize? = DefaultMaxCropSize,
): CropResult = crop(maxResultSize) { file.toImageSrc() }

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [uri] will be used as a source.
 * Set [cacheBeforeUse] to false if you're certain that reopening it multiple times won't be a problem,
 * true otherwise.
 */
suspend fun ImageCropper.crop(
    uri: Uri,
    context: Context,
    maxResultSize: IntSize? = DefaultMaxCropSize,
    cacheBeforeUse: Boolean = true
): CropResult = cacheUri(enabled = cacheBeforeUse, uri, context) { cached ->
    crop(maxResultSize) { cached?.toImageSrc(context) }
}

/**
 * Initiates a new crop session, cancelling the current one, if any.
 * Suspends until a result is available (cancellation, error, success) and returns it.
 * The resulting image will be scaled down to fit [maxResultSize] if provided.
 * [dataSource] will be used as a source.
 * Set [cacheBeforeUse] to false if you're certain that reopening it multiple times won't be a problem,
 * true otherwise.
 */
suspend fun ImageCropper.crop(
    dataSource: ImageStream,
    context: Context,
    maxResultSize: IntSize? = DefaultMaxCropSize,
    cacheBeforeUse: Boolean = true
): CropResult = cacheStream(enabled = cacheBeforeUse, dataSource, context) { cached ->
    crop(maxResultSize) {
        cached?.let {
            ImageStreamSrc(cached)
        }
    }
}

suspend fun <R> cacheUri(
    enabled: Boolean,
    uri: Uri,
    context: Context,
    block: suspend (Uri?) -> R
): R {
    if (!enabled) return block(uri)
    val dst = context.cacheDir.resolve("$CropCacheDir/${UUID.randomUUID()}")
    return try {
        val cached = runCatching { copy(uri, dst, context) }.getOrNull()
        block(cached)
    } finally {
        dst.deleteInBackground()
    }
}

suspend fun <R> cacheStream(
    enabled: Boolean,
    dataSource: ImageStream,
    context: Context,
    block: suspend (ImageStream?) -> R
): R {
    if (!enabled) return block(dataSource)
    val dst = context.cacheDir.resolve("$CropCacheDir/${UUID.randomUUID()}")
    return try {
        val cached = runCatching { copy(dataSource, dst) }.getOrNull()
        val stream = cached?.let { UriImageStream(cached, context) }
        block(stream)
    } finally {
        dst.deleteInBackground()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun File.deleteInBackground() {
    GlobalScope.launch(Dispatchers.IO) { runCatching { delete() } }
}

suspend fun copy(src: Uri, dst: File, context: Context) = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(src)!!.use { srcStream ->
        copy(srcStream, dst)
    }
}

private suspend fun copy(src: ImageStream, dst: File) = src.tryUse { srcStream ->
    copy(srcStream, dst)
}

private fun copy(srcStream: InputStream, dst: File): Uri {
    dst.parentFile?.mkdirs()
    dst.outputStream().use { dstStream ->
        srcStream.copyTo(dstStream)
    }
    return Uri.fromFile(dst)
}