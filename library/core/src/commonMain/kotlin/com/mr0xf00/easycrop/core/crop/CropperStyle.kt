package com.mr0xf00.easycrop.core.crop

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Image Aspect ratio. eg : AspectRatio(16, 9) */
data class AspectRatio(val x: Int, val y: Int)

data class CropperStyleGuidelines(
    val count: Int = 2,
    val color: Color = Color.White,
    val width: Dp = .7f.dp,
)

/**
 * Style provider for the image cropper.
 */
@Stable
interface CropperStyle {
    /** Backdrop for the image */
    val backgroundColor: Color

    /** Overlay color for regions outside of the crop rect */
    val overlayColor: Color

    /** Draws the crop rect [region], including the border and handles */
    fun DrawScope.drawCropRect(region: Rect)


    /** Relative positions of the handles used for transforming the crop rect */
    val handles: List<Offset>

    /** The maximum distance between a handle's center and the touch position
     * for it to be selected */
    val touchRad: Dp

    /** All available crop shapes */
    val shapes: List<CropShape>?

    /** All available aspect ratios */
    val aspects: List<AspectRatio>

    /** Whether the view needs to be zoomed in automatically
     * to fit the crop rect after any change */
    val autoZoom: Boolean
}

val DefaultCropperStyle: CropperStyle by lazy { CropperStyle() }

val LocalCropperStyle = staticCompositionLocalOf { DefaultCropperStyle }

val MainHandles = listOf(
    Offset(0f, 0f), Offset(1f, 1f),
    Offset(1f, 0f), Offset(0f, 1f)
)

val SecondaryHandles = listOf(
    Offset(.5f, 0f), Offset(1f, .5f),
    Offset(.5f, 1f), Offset(0f, .5f)
)

val AllHandles = MainHandles + SecondaryHandles

val DefaultAspectRatios = listOf(
    AspectRatio(1, 1),
    AspectRatio(16, 9),
    AspectRatio(4, 3)
)

/** Creates a [CropperStyle] instance with the default behavior. */
fun CropperStyle(
    backgroundColor: Color = Color.Black,
    rectColor: Color = Color.White,
    rectStrokeWidth: Dp = 2.dp,
    touchRad: Dp = 20.dp,
    guidelines: CropperStyleGuidelines? = CropperStyleGuidelines(),
    secondaryHandles: Boolean = true,
    overlay: Color = Color.Black.copy(alpha = .5f),
    shapes: List<CropShape>? = DefaultCropShapes,
    aspects: List<AspectRatio> = DefaultAspectRatios,
    autoZoom: Boolean = true,
): CropperStyle = object : CropperStyle {
    override val touchRad: Dp get() = touchRad
    override val backgroundColor: Color get() = backgroundColor
    override val overlayColor: Color get() = overlay
    override val shapes: List<CropShape>? get() = shapes?.takeIf { it.isNotEmpty() }
    override val aspects get() = aspects
    override val autoZoom: Boolean get() = autoZoom

    override fun DrawScope.drawCropRect(region: Rect) {
        val strokeWidth = rectStrokeWidth.toPx()
        val finalRegion = region.inflate(strokeWidth / 2)
        if (finalRegion.isEmpty) return
        if (guidelines != null && guidelines.count > 0) {
            drawGuidelines(guidelines, finalRegion)
        }
        drawRect(
            color = rectColor, style = Stroke(strokeWidth),
            topLeft = finalRegion.topLeft, size = finalRegion.size
        )
        drawHandles(finalRegion)
    }

    override val handles: List<Offset> = if (!secondaryHandles) MainHandles else AllHandles

    fun DrawScope.drawHandles(region: Rect) {
        val strokeWidth = (rectStrokeWidth * 3).toPx()
        val rad = touchRad.toPx() / 2
        val cap = StrokeCap.Round

        handles.forEach { (xRel, yRel) ->
            val x = region.left + xRel * region.width
            val y = region.top + yRel * region.height
            when {
                xRel != .5f && yRel != .5f -> {
                    drawCircle(color = rectColor, radius = rad, center = Offset(x, y))
                }
                xRel == 0f || xRel == 1f -> if (region.height > rad * 4) drawLine(
                    rectColor, strokeWidth = strokeWidth,
                    start = Offset(x, (y - rad)),
                    end = Offset(x, (y + rad)), cap = cap
                )
                yRel == 0f || yRel == 1f -> if (region.width > rad * 4) drawLine(
                    rectColor, strokeWidth = strokeWidth,
                    start = Offset((x - rad), y),
                    end = Offset((x + rad), y), cap = cap
                )
            }
        }
    }

    fun DrawScope.drawGuidelines(
        guidelines: CropperStyleGuidelines,
        region: Rect
    ) = clipRect(rect = region) {
        val strokeWidth = guidelines.width.toPx()
        val xStep = region.width / (guidelines.count + 1)
        val yStep = region.height / (guidelines.count + 1)
        for (i in 1..guidelines.count) {
            val x = region.left + i * xStep
            val y = region.top + i * yStep
            drawLine(
                color = guidelines.color, strokeWidth = strokeWidth,
                start = Offset(x, 0f), end = Offset(x, size.height)
            )
            drawLine(
                color = guidelines.color, strokeWidth = strokeWidth,
                start = Offset(0f, y), end = Offset(size.width, y)
            )
        }
    }
}

inline fun DrawScope.clipRect(
    rect: Rect,
    op: ClipOp = ClipOp.Intersect,
    block: DrawScope.() -> Unit
) {
    clipRect(
        left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom,
        clipOp = op, block = block
    )
}
