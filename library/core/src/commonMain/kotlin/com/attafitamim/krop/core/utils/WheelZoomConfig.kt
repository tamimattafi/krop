package com.attafitamim.krop.core.utils

import androidx.compose.runtime.Stable

@Stable
interface WheelZoomConfig {
    /**
     * Enable using trackpad/mouse wheel to ZOOM.
     */
    val enabled: Boolean

    /**
     * Which keyboard modifiers enable wheel-to-zoom. Default Ctrl or Meta (Cmd) like in browsers.
     */
    val trigger: WheelZoomTrigger

    /**
     * Multiplicative zoom speed; higher = faster zoom per wheel tick.
     * Used as exponent factor in exp(-dy * speed) to keep scale > 0.
     */
    val speed: Float

    /**
     * Idle timeout after the last wheel event to consider the gesture complete and call onDone().
     */
    val endTimeoutMillis: Long
}

@Stable
fun wheelZoomConfig(
    enabled: Boolean = false,
    trigger: WheelZoomTrigger = WheelZoomTrigger.CtrlOrMeta,
    speed: Float = 0.15f,
    endTimeoutMillis: Long = 180L,
) = object : WheelZoomConfig {
    override val enabled: Boolean = enabled
    override val trigger: WheelZoomTrigger = trigger
    override val speed: Float = speed
    override val endTimeoutMillis: Long = endTimeoutMillis
}
