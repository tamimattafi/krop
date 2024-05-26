package com.mr0xf00.easycrop.sample.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mr0xf00.easycrop.sample.presentation.ImagesViewModel

@Composable
fun App(viewModel: ImagesViewModel) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            ViewModelDemo(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
            // SimpleDemo(modifier = Modifier.fillMaxSize())
        }
    }
}