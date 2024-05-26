package com.mr0xf00.easycrop.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.mr0xf00.easycrop.core.utils.constrainOffset

private enum class PopupSide {
    Start, End, Top, Bottom
}
private val PopupSide.isHorizontal get() = this == PopupSide.Start || this == PopupSide.End
private fun PopupSide.isLeft(dir: LayoutDirection) =
    (this == PopupSide.Start && dir == LayoutDirection.Ltr) ||
            (this == PopupSide.End && dir == LayoutDirection.Rtl)

@Composable
private fun rememberPopupPos(
    side: PopupSide,
    onAnchorPos: (pos: IntOffset) -> Unit
) = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val popupRect = placePopup(
            rect = popupContentSize.toIntRect(),
            anchor = anchorBounds.inflate(anchorBounds.minDimension / 10),
            side = side,
            dir = layoutDirection
        ).constrainOffset(windowSize.toIntRect())
        onAnchorPos(anchorBounds.center - popupRect.topLeft)
        return popupRect.topLeft
    }
}

private fun placePopup(
    rect: IntRect,
    anchor: IntRect,
    side: PopupSide,
    dir: LayoutDirection
): IntRect {
    val dx = when {
        !side.isHorizontal -> anchor.center.x - rect.center.x
        side.isLeft(dir) -> anchor.left - rect.right
        else -> anchor.right - rect.left
    }
    val dy = when {
        side.isHorizontal -> anchor.center.y - rect.center.y
        side == PopupSide.Top -> anchor.top - rect.bottom
        else -> anchor.bottom - rect.top
    }
    return rect.translate(dx, dy)
}

@Composable
private fun popupShape(anchorPos: IntOffset): Shape {
    val rad = LocalDensity.current.run { 8.dp.toPx() }
    return remember(anchorPos) {
        GenericShape { size, _ ->
            val corners = CornerRadius(size.minDimension * .5f)
            addRoundRect(RoundRect(size.toRect(), corners))
            if (size.width >= rad * 2.1f && size.height >= rad * 2.1f) {
                val indicator = createIndicator(size, anchorPos, rad)
                addPath(indicator)
            }
        }
    }
}

private fun createIndicator(shapeSize: Size, anchor: IntOffset, rad: Float): Path {
    val x = anchor.x.toFloat().coerceIn(rad, shapeSize.width - rad)
    val y = anchor.y.toFloat().coerceIn(rad, shapeSize.height - rad)
    val (from, vec) = when {
        anchor.y < 0 -> Offset(x, 0f) to Offset(0f, -1f)
        anchor.y > shapeSize.height -> Offset(x, shapeSize.height) to Offset(0f, 1f)
        anchor.x < 0 -> Offset(0f, y) to Offset(-1f, 0f)
        anchor.x > shapeSize.width -> Offset(shapeSize.width, y) to Offset(1f, 0f)
        else -> return Path()
    }
    val tan = Offset(-vec.y, vec.x)
    return Path().apply {
        (from + tan * rad).let { moveTo(it.x, it.y) }
        (from + vec * (rad)).let { lineTo(it.x, it.y) }
        (from - tan * rad).let { lineTo(it.x, it.y) }
        close()
    }
}

@Composable
fun OptionsPopup(
    onDismiss: () -> Unit,
    optionCount: Int,
    option: @Composable (Int) -> Unit,
) {
    var anchorPos by remember { mutableStateOf(IntOffset.Zero) }
    val isVertical = LocalVerticalControls.current
    val side = if (isVertical) PopupSide.Start else PopupSide.Top
    Popup(
        onDismissRequest = onDismiss,
        popupPositionProvider = rememberPopupPos(side = side) { anchorPos = it }
    ) {
        Surface(
            shape = popupShape(anchorPos = anchorPos),
            elevation = 8.dp,
        ) {
            if (isVertical) LazyColumn {
                items(optionCount) { i -> option(i) }
            } else LazyRow {
                items(optionCount) { i -> option(i) }
            }
        }
    }
}