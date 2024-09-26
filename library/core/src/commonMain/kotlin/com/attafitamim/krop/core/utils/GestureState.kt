package com.attafitamim.krop.core.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.max
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

interface GestureState {
    val zoom: ZoomState
    val drag: DragState
    val tap: TapState
}

interface DragState {
    fun onBegin(x: Float, y: Float) = Unit
    fun onNext(dx: Float, dy: Float, x: Float, y: Float, pointers: Int) = Unit
    fun onDone() = Unit
}

inline fun dragState(
    crossinline begin: (pos: Offset) -> Unit = { },
    crossinline done: () -> Unit = {},
    crossinline next: (delta: Offset, pos: Offset, pointers: Int) -> Unit = { _, _, _ -> },
): DragState = object : DragState {
    override fun onBegin(x: Float, y: Float) = begin(Offset(x, y))
    override fun onNext(dx: Float, dy: Float, x: Float, y: Float, pointers: Int) =
        next(Offset(dx, dy), Offset(x, y), pointers)

    override fun onDone() = done()
}

interface TapState {
    fun onTap(x: Float, y: Float, pointers: Int) = Unit
    fun onLongPress(x: Float, y: Float, pointers: Int) = Unit
}

inline fun tapState(
    crossinline longPress: (pos: Offset, pointers: Int) -> Unit = { _, _ -> },
    crossinline tap: (pos: Offset, pointers: Int) -> Unit = { _, _ -> },
) = object : TapState {
    override fun onTap(x: Float, y: Float, pointers: Int) = tap(Offset(x, y), pointers)
    override fun onLongPress(x: Float, y: Float, pointers: Int) = longPress(Offset(x, y), pointers)
}

interface ZoomState {
    fun onBegin(cx: Float, cy: Float) = Unit
    fun onNext(scale: Float, cx: Float, cy: Float) = Unit
    fun onDone() = Unit
}

inline fun zoomState(
    crossinline begin: (center: Offset) -> Unit = { },
    crossinline done: () -> Unit = {},
    crossinline next: (scale: Float, center: Offset) -> Unit = { _, _ -> },
): ZoomState = object : ZoomState {
    override fun onBegin(cx: Float, cy: Float) = begin(Offset(cx, cy))
    override fun onNext(scale: Float, cx: Float, cy: Float) = next(scale, Offset(cx, cy))
    override fun onDone() = done()
}

@Composable
fun rememberGestureState(
    zoom: ZoomState? = null,
    drag: DragState? = null,
    tap: TapState? = null,
): GestureState {
    val zoomState by rememberUpdatedState(newValue = zoom ?: object : ZoomState {})
    val dragState by rememberUpdatedState(newValue = drag ?: object : DragState {})
    val tapState by rememberUpdatedState(newValue = tap ?: object : TapState {})
    return object : GestureState {
        override val zoom: ZoomState get() = zoomState
        override val drag: DragState get() = dragState
        override val tap: TapState get() = tapState
    }
}

private data class GestureData(
    var dragId: PointerId = PointerId(-1),
    var firstPos: Offset = Offset.Unspecified,
    var pos: Offset = Offset.Unspecified,
    var nextPos: Offset = Offset.Unspecified,
    var pointers: Int = 0,
    var maxPointers: Int = 0,
    var isDrag: Boolean = false,
    var isZoom: Boolean = false,
    var isTap: Boolean = false,
)


fun Modifier.onGestures(state: GestureState): Modifier {
    var info = GestureData()
    return pointerInput(Unit) {
        coroutineScope {
            launch {
                detectTapGestures( // Note: currently unused
                    onLongPress = { state.tap.onLongPress(it.x, it.y, info.maxPointers) },
                    onTap = { state.tap.onTap(it.x, it.y, info.maxPointers) },
                )
            }
            detectTransformGestures(panZoomLock = true) { c, _, zoom, _ ->
                if (!(info.isDrag || info.isZoom)) {
                    if (info.pointers == 1) {
                        state.drag.onBegin(info.firstPos.x, info.firstPos.y)
                        info.pos = info.firstPos
                        info.isDrag = true
                    } else if (info.pointers > 1) {
                        state.zoom.onBegin(c.x, c.y)
                        info.isZoom = true
                    }
                }
                if (info.isDrag) {
                    state.drag.onNext(
                        info.nextPos.x - info.pos.x, info.nextPos.y - info.pos.y,
                        info.nextPos.x, info.nextPos.y, info.pointers
                    )
                    info.pos = info.nextPos
                } else if (info.isZoom) {
                    if (zoom != 1f) state.zoom.onNext(zoom, c.x, c.y)
                }
            }
        }
    }.pointerInput(Unit) {
        awaitEachGesture {
            info = GestureData()
            val first = awaitFirstDown(requireUnconsumed = false)
            info.dragId = first.id
            info.firstPos = first.position
            info.pointers = 1
            info.maxPointers = 1
            var event: PointerEvent
            while (info.pointers > 0) {
                event = awaitPointerEvent(pass = PointerEventPass.Initial)
                var dragPointer: PointerInputChange? = null
                for (change in event.changes) {
                    if (change.changedToDown()) info.pointers++
                    else if (change.changedToUp()) info.pointers--
                    info.maxPointers = max(info.maxPointers, info.pointers)
                    if (change.id == info.dragId) dragPointer = change
                }
                if (dragPointer == null) dragPointer =
                    event.changes.firstOrNull { it.pressed }
                if (dragPointer != null) {
                    info.nextPos = dragPointer.position
                    if (info.dragId != dragPointer.id) {
                        info.pos = info.nextPos
                        info.dragId = dragPointer.id
                    }
                }
            }
            if (info.isDrag) state.drag.onDone()
            if (info.isZoom) state.zoom.onDone()
        }
    }
}