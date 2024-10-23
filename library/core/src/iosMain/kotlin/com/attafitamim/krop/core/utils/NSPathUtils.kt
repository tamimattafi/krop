package com.attafitamim.krop.core.utils

import platform.Foundation.NSURL

fun String.toNSURL() = NSURL.fileURLWithPath(path = this, isDirectory = false)
