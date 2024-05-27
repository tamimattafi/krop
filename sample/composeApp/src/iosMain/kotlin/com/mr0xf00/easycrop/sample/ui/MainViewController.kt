package com.mr0xf00.easycrop.sample.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.mr0xf00.easycrop.sample.presentation.ImagesViewModel

fun MainViewController() = ComposeUIViewController { App(ImagesViewModel()) }