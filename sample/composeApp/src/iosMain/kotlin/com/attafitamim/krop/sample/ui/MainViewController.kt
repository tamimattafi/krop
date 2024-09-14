package com.attafitamim.krop.sample.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.attafitamim.krop.sample.presentation.ImagesViewModel

fun MainViewController() = ComposeUIViewController { App(ImagesViewModel()) }