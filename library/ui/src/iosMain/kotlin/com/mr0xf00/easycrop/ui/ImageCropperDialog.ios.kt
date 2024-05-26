package com.mr0xf00.easycrop.ui

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

@Composable
actual fun isLandscape(): Boolean = when (UIDevice.currentDevice.orientation) {
    UIDeviceOrientation.UIDeviceOrientationLandscapeRight,
    UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> true
    else -> false
}