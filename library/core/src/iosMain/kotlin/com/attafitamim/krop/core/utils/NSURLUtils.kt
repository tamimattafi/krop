package com.attafitamim.krop.core.utils

import androidx.compose.ui.unit.IntSize
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFURLRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSDictionary
import platform.Foundation.NSURL
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateWithURL

fun String.toNSURL() = NSURL.fileURLWithPath(path = this, isDirectory = false)

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalForeignApi::class)
fun NSURL.getImageSize(): IntSize? {
    val typeRef = CFBridgingRetain(this)
    val source = kotlin.runCatching {
        val cfUrl = typeRef as? CFURLRef
        CGImageSourceCreateWithURL(url = cfUrl, options = null)
    }.getOrNull()

    CFRelease(typeRef)

    val dictionaryRef = CGImageSourceCopyPropertiesAtIndex(
        isrc = source,
        index = 0u,
        options = null
    )

    val dictionary = CFBridgingRelease(dictionaryRef) as? NSDictionary ?: return null
    val width = dictionary.objectForKey("PixelWidth") as? Int ?: return null
    val height = dictionary.objectForKey("PixelHeight") as? Int ?: return null
    return IntSize(width, height).validateSize()
}