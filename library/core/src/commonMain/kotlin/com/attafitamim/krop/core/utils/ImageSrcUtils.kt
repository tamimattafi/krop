package com.attafitamim.krop.core.utils

import androidx.compose.ui.unit.IntSize

fun IntSize.validateSize(): IntSize? = takeIf {
    it.width > 0 && it.height > 0
}
