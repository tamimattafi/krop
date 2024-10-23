@file:OptIn(ExperimentalForeignApi::class)

package com.attafitamim.krop.sample.picker

import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.UIImageSrc
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

class ImagePickerDelegate(
    private val onImage: (uri: ImageSrc) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: return

        val imageSrc = UIImageSrc(image) ?: return
        picker.dismissViewControllerAnimated(true, null)
        onImage.invoke(imageSrc)
    }
}