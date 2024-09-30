package com.attafitamim.krop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropShape
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.LocalCropperStyle
import com.attafitamim.krop.core.crop.flipHorizontal
import com.attafitamim.krop.core.crop.flipVertical
import com.attafitamim.krop.core.crop.rotLeft
import com.attafitamim.krop.core.crop.rotRight
import com.attafitamim.krop.core.utils.eq0
import com.attafitamim.krop.core.utils.setAspect
import krop.library.ui.generated.resources.Res
import krop.library.ui.generated.resources.flip_hor
import krop.library.ui.generated.resources.flip_ver
import krop.library.ui.generated.resources.resize
import krop.library.ui.generated.resources.rot_left
import krop.library.ui.generated.resources.rot_right
import org.jetbrains.compose.resources.painterResource

fun Size.isAspect(aspect: AspectRatio): Boolean {
    return ((width / height) - (aspect.x.toFloat() / aspect.y)).eq0()
}

val LocalVerticalControls = staticCompositionLocalOf { false }

@Composable
fun CropperControls(
    isVertical: Boolean,
    state: CropState,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalVerticalControls provides isVertical) {
        ButtonsBar(modifier = modifier) {
            IconButton(onClick = { state.rotLeft() }) {
                Icon(painterResource(Res.drawable.rot_left), null)
            }
            IconButton(onClick = { state.rotRight() }) {
                Icon(painterResource(Res.drawable.rot_right), null)
            }
            IconButton(onClick = { state.flipHorizontal() }) {
                Icon(painterResource(Res.drawable.flip_hor), null)
            }
            IconButton(onClick = { state.flipVertical() }) {
                Icon(painterResource(Res.drawable.flip_ver), null)
            }
            LocalCropperStyle.current.aspects.let { aspects ->
                if (aspects.size > 1) {
                    Box {
                        var menu by remember { mutableStateOf(false) }
                        IconButton(onClick = { menu = !menu }) {
                            Icon(painterResource(Res.drawable.resize), null)
                        }
                        if (menu) AspectSelectionMenu(
                            onDismiss = { menu = false },
                            region = state.region,
                            onRegion = { state.region = it },
                            lock = state.aspectLock,
                            onLock = { state.aspectLock = it }
                        )
                    }
                }
            }
            LocalCropperStyle.current.shapes.let { shapes ->
                if (shapes.size > 1) {
                    Box {
                        var menu by remember { mutableStateOf(false) }
                        IconButton(onClick = { menu = !menu }) {
                            Icon(Icons.Default.Star, null)
                        }
                        if (menu) ShapeSelectionMenu(
                            onDismiss = { menu = false },
                            selected = state.shape,
                            onSelect = { state.shape = it },
                            shapes = shapes
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonsBar(
    modifier: Modifier = Modifier,
    buttons: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        elevation = 4.dp,
        color = MaterialTheme.colors.surface.copy(alpha = .8f),
        contentColor = contentColorFor(MaterialTheme.colors.surface)
    ) {
        if (LocalVerticalControls.current) Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            buttons()
        } else Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            buttons()
        }
    }
}


@Composable
fun ShapeSelectionMenu(
    onDismiss: () -> Unit,
    shapes: List<CropShape>,
    selected: CropShape,
    onSelect: (CropShape) -> Unit,
) {
    OptionsPopup(onDismiss = onDismiss, optionCount = shapes.size) { i ->
        val shape = shapes[i]
        ShapeItem(shape = shape, selected = selected == shape,
            onSelect = { onSelect(shape) })
    }
}


@Composable
fun ShapeItem(
    shape: CropShape, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (!selected) LocalContentColor.current
        else MaterialTheme.colors.secondaryVariant
    )
    IconButton(
        modifier = modifier,
        onClick = onSelect
    ) {
        val shapeState by rememberUpdatedState(newValue = shape)
        Box(modifier = Modifier
            .size(20.dp)
            .drawWithCache {
                val path = shapeState.asPath(size.toRect())
                val strokeWidth = 2.dp.toPx()
                onDrawWithContent {
                    drawPath(path = path, color = color, style = Stroke(strokeWidth))
                }
            })
    }
}


@Composable
fun AspectSelectionMenu(
    onDismiss: () -> Unit,
    region: Rect,
    onRegion: (Rect) -> Unit,
    lock: Boolean,
    onLock: (Boolean) -> Unit,
) {
    val aspects = LocalCropperStyle.current.aspects
    OptionsPopup(onDismiss = onDismiss, optionCount = 1 + aspects.size) { i ->
        val unselectedTint = LocalContentColor.current
        val selectedTint = MaterialTheme.colors.secondaryVariant
        if (i == 0) IconButton(onClick = { onLock(!lock) }) {
            Icon(
                Icons.Default.Lock, null,
                tint = if (lock) selectedTint else unselectedTint
            )
        } else {
            val aspect = aspects[i - 1]
            val isSelected = region.size.isAspect(aspect)
            IconButton(onClick = { onRegion(region.setAspect(aspect)) }) {
                Text(
                    "${aspect.x}:${aspect.y}",
                    color = if (isSelected) selectedTint else unselectedTint
                )
            }
        }
    }
}