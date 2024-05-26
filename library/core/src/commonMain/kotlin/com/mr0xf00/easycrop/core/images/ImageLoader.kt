package com.mr0xf00.easycrop.core.images

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.mr0xf00.easycrop.core.utils.fitIn
import com.mr0xf00.easycrop.core.utils.setRectToRect
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun rememberLoadedImage(src: ImageSrc, view: IntSize, imgToView: Matrix): DecodeResult? {
    // TODO provide better caching system
    var full by remember { mutableStateOf<DecodeResult?>(null) }
    var enhanced by remember { mutableStateOf<DecodeResult?>(null) }
    LaunchedEffect(src, view) {
        val fullMat = Matrix().apply {
            val imgRect = src.size.toSize().toRect()
            setRectToRect(imgRect, imgRect.fitIn(view.toSize().toRect()))
        }
        val fullParams = getDecodeParams(view, src.size, fullMat)
        if (fullParams != null) full = src.open(fullParams)
    }
    LaunchedEffect(src, view, imgToView, full == null) decode@{
        if (full == null) return@decode
        if (enhanced == null) yield()
        val params = getDecodeParams(view, src.size, imgToView) ?: return@decode
        if (enhanced?.params?.contains(params) == true) return@decode
        if (full?.params?.contains(params) == true) {
            enhanced = full
            return@decode
        }
        enhanced = null
        delay(500)
        enhanced = src.open(params)
    }
    return enhanced ?: full
}
