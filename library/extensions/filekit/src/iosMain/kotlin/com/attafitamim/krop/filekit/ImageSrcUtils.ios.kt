package com.attafitamim.krop.filekit

import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.utils.UIImageSrc
import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIImage

actual suspend fun PlatformFile.toImageSrc(): ImageSrc? {
    val data = NSData.dataWithContentsOfURL(nsUrl) ?: return null
    val uiImage = UIImage(data = data)
    return UIImageSrc(uiImage)
}
