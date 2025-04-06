@file:OptIn(ExperimentalMaterial3Api::class)

package com.attafitamim.krop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.CropperStyle
import com.attafitamim.krop.core.crop.DefaultCropperStyle
import com.attafitamim.krop.core.crop.LocalCropperStyle
import krop.library.ui.generated.resources.Res
import krop.library.ui.generated.resources.restore
import org.jetbrains.compose.resources.painterResource

val CropperDialogProperties = (DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false
))

@Composable
expect fun isVerticalPickerControls(): Boolean

@Composable
fun ImageCropperDialog(
    state: CropState,
    style: CropperStyle = DefaultCropperStyle,
    dialogProperties: DialogProperties = CropperDialogProperties,
    dialogPadding: PaddingValues = PaddingValues(16.dp),
    dialogShape: Shape = RoundedCornerShape(8.dp),
    topBar: @Composable (CropState) -> Unit = { DefaultTopBar(it) },
    cropControls: @Composable BoxScope.(CropState) -> Unit = { DefaultControls(it) }
) {
    LaunchedEffect(Unit) {
        state.setInitialState(style) // Could be buggy, since it is run in a separate thread
    }

    CompositionLocalProvider(LocalCropperStyle provides style) {
        Dialog(
            onDismissRequest = { state.done(accept = false) },
            properties = dialogProperties,
        ) {
            Surface(
                modifier = Modifier.padding(dialogPadding),
                shape = dialogShape,
            ) {
                Column {
                    topBar(state)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()
                    ) {
                        CropperPreview(state = state, modifier = Modifier.fillMaxSize())
                        cropControls(state)
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.DefaultControls(state: CropState) {
    val verticalControls = isVerticalPickerControls()
    CropperControls(
        isVertical = verticalControls,
        state = state,
        modifier = Modifier
            .align(if (!verticalControls) Alignment.BottomCenter else Alignment.CenterEnd)
            .padding(12.dp),
    )
}

@Composable
fun DefaultTopBar(state: CropState) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { state.done(accept = false) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        actions = {
            IconButton(onClick = { state.reset() }) {
                Icon(painterResource(Res.drawable.restore), null)
            }
            IconButton(onClick = { state.done(accept = true) }, enabled = !state.accepted) {
                Icon(Icons.Default.Done, null)
            }
        }
    )
}
