package com.attafitamim.krop.core.utils

import androidx.compose.ui.unit.IntSize
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.NSURL
import platform.Photos.PHAsset
import platform.Photos.PHAssetCollection
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHContentEditingInputRequestOptions
import platform.Photos.requestContentEditingInputWithOptions

fun PHAsset.getSize(): IntSize? = IntSize(
    pixelWidth.toInt(),
    pixelHeight.toInt()
).validateSize()

fun loadPHAsset(localIdentifier: String): PHAsset? {
    val results = PHAssetCollection.fetchAssetCollectionsWithLocalIdentifiers(
        identifiers = listOf(localIdentifier),
        options = null
    )

    // TODO: check if results will always return list of PHAsset and not Collection
    return results.firstObject as? PHAsset
}

suspend fun PHAsset.getNSURL(): NSURL? = suspendCoroutine { continuation ->
    when (mediaType) {
        PHAssetMediaTypeImage -> {
            val options = PHContentEditingInputRequestOptions()
            options.setNetworkAccessAllowed(true)
            options.setCanHandleAdjustmentData {
                true
            }

            requestContentEditingInputWithOptions(options) { contentEditingInput, _ ->
                val nsURL = contentEditingInput?.fullSizeImageURL
                continuation.resume(nsURL)
            }
        }

        else -> continuation.resume(null)
    }
}
