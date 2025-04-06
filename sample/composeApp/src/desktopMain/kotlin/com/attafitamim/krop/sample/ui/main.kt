package com.attafitamim.krop.sample.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.attafitamim.krop.sample.presentation.ImagesViewModel
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    // Initialize FileKit
    FileKit.init("KropSample")

    Window(onCloseRequest = ::exitApplication, title = "Krop Sample") {
        App(ImagesViewModel())
    }
}
