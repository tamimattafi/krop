package com.attafitamim.krop.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropperLoading

@Composable
fun CropErrorDialog(error: CropError, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Ok") } },
        text = { Text(error.getMessage()) }
    )
}

@Composable
fun CropError.getMessage(): String = remember(this) {
    when (this) {
        CropError.LoadingError -> "Error while opening the image !"
        CropError.SavingError -> "Error while saving the image !"
    }
}


@Composable
fun LoadingDialog(status: CropperLoading) {
    var dismissed by remember(status) { mutableStateOf(false) }
    if (!dismissed) Dialog(onDismissRequest = { dismissed = true }) {
        Surface(shape = MaterialTheme.shapes.small, shadowElevation = 6.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                CircularProgressIndicator()
                Text(text = status.getMessage())
            }
        }
    }
}

@Composable
fun CropperLoading.getMessage(): String {
    return remember(this) {
        when (this) {
            CropperLoading.PreparingImage -> "Preparing Image"
            CropperLoading.SavingResult -> "Saving Result"
        }
    }
}
