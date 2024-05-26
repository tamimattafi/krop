package com.mr0xf00.easycrop.ui

import com.mr0xf00.easycrop.core.images.ImageBitmapSrc
import com.mr0xf00.easycrop.core.images.ImageSrc
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

        val imageBitmap = image?.toImageBitmap() ?: return
        val imageSrc = ImageBitmapSrc(imageBitmap)
        picker.dismissViewControllerAnimated(true, null)
        onImage.invoke(imageSrc)
    }
}