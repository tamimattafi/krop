package com.attafitamim.krop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

@Composable
actual fun isVerticalPickerControls(): Boolean {
    val orientation = remember {
        UIDevice.currentDevice.orientation
    }

    return when (orientation) {
        UIDeviceOrientation.UIDeviceOrientationLandscapeRight,
        UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> true
        else -> false
    }
}
