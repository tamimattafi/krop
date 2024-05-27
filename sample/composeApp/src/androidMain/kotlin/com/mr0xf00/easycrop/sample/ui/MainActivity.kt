package com.mr0xf00.easycrop.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mr0xf00.easycrop.sample.presentation.ImagesViewModel
import com.mr0xf00.easycrop.sample.ui.App

class MainActivity : ComponentActivity() {
    private val viewModel: ImagesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(viewModel)
        }
    }
}
