package com.mr0xf00.easycrop.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.mr0xf00.easycrop.sample.presentation.ImagesViewModel
import com.mr0xf00.easycrop.sample.ui.App

fun MainViewController() = ComposeUIViewController { App(ImagesViewModel()) }