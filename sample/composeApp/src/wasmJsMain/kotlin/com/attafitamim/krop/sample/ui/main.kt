package com.attafitamim.krop.sample.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.attafitamim.krop.sample.presentation.ImagesViewModel
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App(ImagesViewModel())
    }
}
