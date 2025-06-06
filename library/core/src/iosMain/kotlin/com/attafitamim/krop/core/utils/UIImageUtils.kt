package com.attafitamim.krop.core.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGAffineTransformMakeRotation
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGRectApplyAffineTransform
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSURL
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

internal const val DEFAULT_BITMAP_COMPRESSION_QUALITY = 1.0
private const val DEFAULT_ANGLE = 2 * kotlin.math.PI

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toByteArray(quality: Double): ByteArray? {
    val imageData = UIImageJPEGRepresentation(this, quality)
        ?: return null

    val bytes = imageData.bytes
        ?: return null

    val length = imageData.length

    val data: CPointer<ByteVar> = bytes.reinterpret()
    return ByteArray(length.toInt()) { index -> data[index] }
}

fun UIImage.toImageBitmap(quality: Double): ImageBitmap? {
    val byteArray = toByteArray(quality) ?: return null
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.ensureCorrectOrientation(): UIImage {
    val newSize = CGRectApplyAffineTransform(
        CGRectMake(
            0.0,
            0.0,
            size.useContents { width },
            size.useContents { height }
        ),
        CGAffineTransformMakeRotation(DEFAULT_ANGLE)
    )

    newSize.useContents { size.width = floor(size.width) }
    newSize.useContents { size.height = floor(size.height) }

    UIGraphicsBeginImageContextWithOptions(
        CGSizeMake(
            newSize.useContents { size.width },
            newSize.useContents { size.height }
        ),
        false,
        scale
    )

    val context = UIGraphicsGetCurrentContext() ?: return this

    CGContextTranslateCTM(
        context,
        newSize.useContents { size.width / 2 },
        newSize.useContents { size.height / 2 }
    )
    CGContextRotateCTM(
        context,
        DEFAULT_ANGLE
    )
    drawInRect(
        CGRectMake(
            -size.useContents { width / 2 },
            -size.useContents { height / 2 },
            size.useContents { width },
            size.useContents { height }
        )
    )
    val newUIImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return newUIImage ?: this
}

@ExperimentalForeignApi
fun UIImage.getSize(): IntSize? = size.useContents {
    IntSize(width.roundToInt(), height.roundToInt())
}.validateSize()

fun NSURL.toUIImage(): UIImage? {
    val nsPath = path ?: return null
    return UIImage.imageWithContentsOfFile(nsPath)
        ?.ensureCorrectOrientation()
}
