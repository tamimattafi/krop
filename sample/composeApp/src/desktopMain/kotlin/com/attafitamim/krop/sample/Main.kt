package com.attafitamim.krop.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.attafitamim.krop.sample.presentation.ImagesViewModel
import com.attafitamim.krop.sample.ui.App

fun main() {
    application {
        Window(
            title = "Krop Sample",
            onCloseRequest = ::exitApplication
        ) {
            /**
             * To simplify the sample, we are just initializing the ViewModel as a normal object here.
             */
            val viewModel = ImagesViewModel()
            App(viewModel)
        }
    }
}