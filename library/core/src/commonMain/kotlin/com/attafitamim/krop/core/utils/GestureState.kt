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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.exp
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

    /**
     * Enable using trackpad/mouse wheel to ZOOM.
     */
    val wheelZoomEnabled: Boolean get() = true

    /**
     * Which keyboard modifiers enable wheel-to-zoom. Default Ctrl or Meta (Cmd) like in browsers.
     */
    val wheelZoomTrigger: WheelZoomTrigger get() = WheelZoomTrigger.CtrlOrMeta

    /**
     * Multiplicative zoom speed; higher = faster zoom per wheel tick.
     * Used as exponent factor in exp(-dy * speed) to keep scale > 0.
     */
    val wheelZoomSpeed: Float get() = 0.15f

    /**
     * Idle timeout after the last wheel event to consider the gesture complete and call onDone().
     */
    val wheelEndTimeoutMillis: Long get() = 180L

    fun onBegin(cx: Float, cy: Float) = Unit
    fun onNext(scale: Float, cx: Float, cy: Float) = Unit
    fun onDone() = Unit
}

enum class WheelZoomTrigger {
    Any,
    CtrlOrMeta,
    CtrlOnly,
    MetaOnly,
    AltOnly,
    ShiftOnly,
}

inline fun zoomState(
    crossinline begin: (center: Offset) -> Unit = { },
    crossinline done: () -> Unit = {},
    crossinline next: (scale: Float, center: Offset) -> Unit = { _, _ -> },
    zoomEnabled: Boolean = true,
    zoomTrigger: WheelZoomTrigger = WheelZoomTrigger.CtrlOrMeta,
    zoomSpeed: Float = 0.15f,
    endTimeoutMillis: Long = 180L,
): ZoomState = object : ZoomState {
    override val wheelZoomEnabled: Boolean = zoomEnabled
    override val wheelZoomTrigger: WheelZoomTrigger = zoomTrigger
    override val wheelZoomSpeed: Float = zoomSpeed
    override val wheelEndTimeoutMillis: Long = endTimeoutMillis
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

fun Modifier.onGestures(state: GestureState): Modifier = pointerInput(Unit) {
    var info = GestureData()
    coroutineScope {
        launch {
            detectTapGestures(
                onLongPress = { state.tap.onLongPress(it.x, it.y, info.maxPointers) },
                onTap = { state.tap.onTap(it.x, it.y, info.maxPointers) },
            )
        }

        launch {
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

        launch {
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
                        if (change.changedToDownIgnoreConsumed()) info.pointers++
                        else if (change.changedToUpIgnoreConsumed()) info.pointers--
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

        launch {
            handleScrollWheelZoom(state.zoom, this@coroutineScope)
        }
    }
}

private suspend fun PointerInputScope.handleScrollWheelZoom(
    state: ZoomState,
    coroutineScope: CoroutineScope,
) {
    awaitPointerEventScope {
        var wheelZoomActive = false
        var wheelEndJob: Job? = null

        fun scheduleWheelEnd(onDone: () -> Unit) {
            wheelEndJob?.cancel()
            wheelEndJob = coroutineScope.launch {
                delay(state.wheelEndTimeoutMillis)
                onDone()
            }
        }

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type == PointerEventType.Scroll) {
                var scroll = Offset.Zero
                for (c in event.changes) scroll += c.scrollDelta
                if (scroll != Offset.Zero) {
                    val pos = event.changes.firstOrNull()?.position ?: Offset.Zero

                    val isZoomTrigger = with(event.keyboardModifiers) {
                        when (state.wheelZoomTrigger) {
                            WheelZoomTrigger.Any -> true
                            WheelZoomTrigger.CtrlOrMeta -> isCtrlPressed || isMetaPressed
                            WheelZoomTrigger.CtrlOnly -> isCtrlPressed
                            WheelZoomTrigger.MetaOnly -> isMetaPressed
                            WheelZoomTrigger.AltOnly -> isAltPressed
                            WheelZoomTrigger.ShiftOnly -> isShiftPressed
                        }
                    }

                    val shouldZoom = state.wheelZoomEnabled && isZoomTrigger

                    if (shouldZoom) {
                        if (!wheelZoomActive) {
                            state.onBegin(pos.x, pos.y)
                            wheelZoomActive = true
                        }
                        val scale = exp(-scroll.y * state.wheelZoomSpeed)
                        if (scale != 1f && scale > 0f) {
                            state.onNext(scale, pos.x, pos.y)
                        }
                        scheduleWheelEnd {
                            if (wheelZoomActive) {
                                state.onDone()
                                wheelZoomActive = false
                            }
                        }
                        continue
                    }
                }
            }
        }
    }
}