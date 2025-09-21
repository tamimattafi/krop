package com.attafitamim.krop.core.crop

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import com.attafitamim.krop.core.utils.ViewMat
import com.attafitamim.krop.core.utils.WheelZoomConfig
import com.attafitamim.krop.core.utils.ZoomLimits
import com.attafitamim.krop.core.utils.abs
import com.attafitamim.krop.core.utils.dragState
import com.attafitamim.krop.core.utils.onGestures
import com.attafitamim.krop.core.utils.rememberGestureState
import com.attafitamim.krop.core.utils.resize
import com.attafitamim.krop.core.utils.zoomState

private val MoveHandle = Offset(.5f, .5f)

class DragHandle(
    val handle: Offset,
    val initialPos: Offset,
    val initialRegion: Rect
)

fun Modifier.cropperTouch(
    region: Rect,
    onRegion: (Rect) -> Unit,
    touchRad: Dp,
    handles: List<Offset>,
    viewMat: ViewMat,
    pending: DragHandle?,
    onPending: (DragHandle?) -> Unit,
    zooming: MutableState<Boolean>,
    dragging: MutableState<Boolean>,
    zoomLimits: ZoomLimits,
    wheelZoomConfig: WheelZoomConfig,
): Modifier = composed {
    val touchRadPx2 = LocalDensity.current.run {
        remember(touchRad, viewMat.scale) { touchRad.toPx() / viewMat.scale }.let { it * it }
    }

    onGestures(
        rememberGestureState(
            zoom = zoomState(
                wheelZoomConfig = wheelZoomConfig,
                begin = { c ->
                    zooming.value = true
                    viewMat.zoomStart(c)
                },
                next = { s, c -> viewMat.zoom(c, s, zoomLimits) },
                done = { zooming.value = false }
            ),
            drag = dragState(
                begin = { pos ->
                    val localPos = viewMat.invMatrix.map(pos)
                    val handle = handles.findHandle(
                        region,
                        localPos,
                        touchRadPx2,
                    )

                    dragging.value = handle == null

                    when {
                        dragging.value -> viewMat.dragStart(pos)
                        handle != null -> onPending(DragHandle(handle, localPos, region))
                    }
                },
                next = { _, pos, _ ->
                    when {
                        dragging.value -> viewMat.drag(pos)
                        pending != null -> {
                            val localPos = viewMat.invMatrix.map(pos)
                            val delta = (localPos - pending.initialPos).round().toOffset()
                            val newRegion = if (pending.handle != MoveHandle) {
                                pending.initialRegion.resize(
                                    pending.handle,
                                    delta,
                                    zoomLimits.minCropSize,
                                )
                            } else {
                                pending.initialRegion.translate(delta)
                            }

                            onRegion(newRegion)
                        }
                    }
                },
                done = {
                    when {
                        dragging.value -> dragging.value = false
                        pending != null -> onPending(null)
                    }
                }
            )
        )
    )
}

fun List<Offset>.findHandle(
    region: Rect,
    pos: Offset,
    touchRadPx2: Float
): Offset? {
    firstOrNull { (region.abs(it) - pos).getDistanceSquared() <= touchRadPx2 }?.let { return it }
    if (region.contains(pos)) return MoveHandle
    return null
}