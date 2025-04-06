package com.attafitamim.krop.sample

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.attafitamim.krop.sample.presentation.ImagesViewModel
import com.attafitamim.krop.sample.ui.App
import io.github.vinceglb.filekit.FileKit

fun main() {
    // Initialize FileKit
    FileKit.init("KropSample")

    application {
        Window(
            title = "Krop Sample",
            onCloseRequest = ::exitApplication
        ) {
            /**
             * To simplify the sample, we are just initializing the ViewModel as a normal object here.
             */
            val viewModel = remember { ImagesViewModel() }
            App(viewModel)
        }
    }
}