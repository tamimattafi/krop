package com.attafitamim.krop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.DragHandle
import com.attafitamim.krop.core.crop.ImgTransform
import com.attafitamim.krop.core.crop.LocalCropperStyle
import com.attafitamim.krop.core.crop.animateImgTransform
import com.attafitamim.krop.core.crop.asMatrix
import com.attafitamim.krop.core.crop.cropperTouch
import com.attafitamim.krop.core.images.rememberLoadedImage
import com.attafitamim.krop.core.utils.ViewMat
import com.attafitamim.krop.core.utils.ZoomLimits
import com.attafitamim.krop.core.utils.applyTransformation
import com.attafitamim.krop.core.utils.times
import com.attafitamim.krop.core.utils.viewMat
import kotlinx.coroutines.delay

private const val TouchAreaOffsetPx = 100f

@Composable
fun CropperPreview(
    state: CropState,
    modifier: Modifier = Modifier
) {
    val style = LocalCropperStyle.current
    val imgTransform by animateImgTransform(target = state.transform)
    val imgMat = remember(imgTransform, state.src.size) { imgTransform.asMatrix(state.src.size) }
    val viewMat = remember { viewMat() }
    var view by remember { mutableStateOf(IntSize.Zero) }
    var pendingDrag by remember { mutableStateOf<DragHandle?>(null) }
    val zooming = remember { mutableStateOf(false) }
    val dragging = remember { mutableStateOf(false) }
    val viewPadding = LocalDensity.current.run { style.touchRad.toPx() }
    val totalMat = remember(viewMat.matrix, imgMat) { imgMat * viewMat.matrix }
    val image = rememberLoadedImage(state.src, view, totalMat)
    val cropRect = remember(state.region, viewMat.matrix) {
        viewMat.matrix.map(state.region)
    }
    val cropPath = remember(state.shape, cropRect) { state.shape.asPath(cropRect) }
    val touchRect = remember(cropRect) {
        Rect(
            left = cropRect.left - TouchAreaOffsetPx,
            top = cropRect.top - TouchAreaOffsetPx,
            right = cropRect.right + (TouchAreaOffsetPx * 2),
            bottom = cropRect.bottom + (TouchAreaOffsetPx * 2)
        )
    }
    val zoomLimits = remember(state.src.size, view) {
        ZoomLimits(state.src.size, view)
    }
    BringToView(
        enabled = style.autoZoom,
        hasOverride = pendingDrag != null,
        zooming = zooming.value,
        dragging = zooming.value,
        outer = view.toSize().toRect().deflate(viewPadding),
        mat = viewMat, local = state.region,
        defaultRegion = state.defaultRegion,
        transform = state.transform,
    )
    Canvas(
        modifier = modifier
            .onGloballyPositioned { view = it.size }
            .background(color = style.backgroundColor)
            .disabledSystemGestureArea {
                touchRect
            }
            .cropperTouch(
                region = state.region,
                onRegion = { state.region = it },
                touchRad = style.touchRad, handles = style.handles,
                viewMat = viewMat,
                pending = pendingDrag,
                onPending = { pendingDrag = it },
                zooming = zooming,
                dragging = dragging,
                zoomLimits = zoomLimits,
                wheelZoomConfig = style.wheelZoomConfig,
            )
    ) {
        image?.let { (params, bitmap) ->
            withTransform({ transform(totalMat) }) {
                drawImage(
                    bitmap, dstOffset = params.subset.topLeft,
                    dstSize = params.subset.size
                )
            }
            with(style) {
                clipPath(cropPath, ClipOp.Difference) {
                    drawRect(color = overlayColor)
                }
                drawCropRect(cropRect)
            }
        }
    }
}

@Composable
fun BringToView(
    enabled: Boolean,
    hasOverride: Boolean,
    zooming: Boolean,
    dragging: Boolean,
    outer: Rect,
    mat: ViewMat,
    local: Rect,
    defaultRegion: Rect,
    transform: ImgTransform,
) {
    if (outer.isEmpty) return
    DisposableEffect(Unit) {
        mat.snapFit(mat.matrix.map(local), outer)
        onDispose { }
    }
    if (!enabled) return
    var overrideBlock by remember { mutableStateOf(false) }
    var pendingAutoZoom by remember { mutableStateOf(true) }

    if (zooming || dragging) {
        // avoid annoying snap when user has started zooming immediately after crop window change
        pendingAutoZoom = false
    }

    LaunchedEffect(local, outer) {
        pendingAutoZoom = true // adjusting crop window / resetting crop / device rotation
    }

    LaunchedEffect(outer, transform) { // device rotation
        mat.setOriginalScale(defaultRegion.applyTransformation(transform), outer)
    }

    LaunchedEffect(zooming, dragging, hasOverride, outer, local) autoZoom@{
        if (zooming || dragging || !pendingAutoZoom) {
            return@autoZoom
        }
        if (hasOverride) overrideBlock = true
        else {
            if (overrideBlock) {
                delay(500)
            }
            mat.fit(mat.matrix.map(local), outer)
            overrideBlock = false
            pendingAutoZoom = false
        }
    }
}
