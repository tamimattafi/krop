package com.attafitamim.krop.sample.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.attafitamim.krop.sample.presentation.ImagesViewModel

@Composable
fun App(viewModel: ImagesViewModel) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            /*ViewModelDemo(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )*/
            SimpleDemo(modifier = Modifier.fillMaxSize())
        }
    }
}