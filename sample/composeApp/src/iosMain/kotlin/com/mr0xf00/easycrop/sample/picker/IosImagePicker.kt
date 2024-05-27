package com.mr0xf00.easycrop.sample.picker

import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol

class IosImagePicker(
    private val controller: UIImagePickerController,
    private val delegate: UINavigationControllerDelegateProtocol
) : ImagePicker {
    override fun pick(mimetype: String) {
        controller.setSourceType(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
        controller.setAllowsEditing(false)
        controller.setDelegate(delegate)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            controller, true, null
        )
    }
}

