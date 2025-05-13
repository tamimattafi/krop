<h1 align="center">
    <img height="150" src="./art/icon.png"/>
    <br>
    Krop for Kotlin and Compose Multiplatform
</h1>
<p align="center">Easy to use image cropping library for Kotlin and Compose Multiplatform, with support for shapes, aspect-ratios, transformations, large images, auto zoom...</p>

[![Krop Release](https://img.shields.io/github/release/tamimattafi/krop.svg?style=for-the-badge&color=darkgreen)](https://github.com/tamimattafi/krop/releases)
[![Kotlin](https://img.shields.io/github/languages/top/tamimattafi/krop.svg?style=for-the-badge&color=blueviolet)](https://kotlinlang.org/)
[![License Apache 2.0](https://img.shields.io/github/license/tamimattafi/krop.svg?style=for-the-badge&color=purple)](https://github.com/tamimattafi/krop/blob/main/LICENSE)

## Demo
Krop supports the following targets: `android`, `ios`, `jvm/desktop`, `js/browser`, `wasmJs`

<p align="center">
<img height="500" src="art/preview.gif"/>
</p>

## Getting Started

### 1. Add Dependencies
Version: 

[![Krop Release](https://img.shields.io/github/release/tamimattafi/krop.svg?style=for-the-badge&color=darkgreen)](https://github.com/tamimattafi/krop/releases)

Compatibility:

| Krop version | Kotlin version | Compose version |
|--------------|----------------|-----------------|
| 0.2.0        | 2.1.21         | 1.8.0           |
| 0.1.4        | 2.0            | 1.7             |
| 0.1.2        | 2.0            | 1.6             |
| 0.1.0        | 1.9            | 1.6             |

**Option 1:** Add the `ui` module to use the crop dialog out of the box:
```kotlin
commonMain.dependencies {
    implementation("com.attafitamim.krop:ui:$version")
    
    // Optional: use extensions for different 3rd party libraries
    implementation("com.attafitamim.krop:extensions-filekit:$version")
}
```

**Option 2:** If you are looking for a custom design, use the `core` module instead:
```kotlin
commonMain.dependencies {
    implementation("com.attafitamim.krop:core:$version")
}
```
For hints on how to use `core` logic for a custom design, check sources of the `ui` module.

### 2. Create an `ImageCropper` instance
#### ***Option 1 : inside the composition***
```kotlin
val imageCropper = rememberImageCropper()
```
#### ***Option 2 : outside the composition (eg. ViewModel)***
```kotlin
class MyViewModel : ViewModel {
    val imageCropper = ImageCropper()
}
```
### 3. Trigger crop action through `imageCropper`
```kotlin
scope.launch {
    val result = imageCropper.crop(bitmap) // Suspends until user accepts or cancels cropping
    when (result) {
        CropResult.Cancelled -> { }
        is CropError -> { }
        is CropResult.Success -> { result.bitmap }
    }
}
```
### 4. Use the state from `imageCropper` to open the crop dialog
```kotlin
val cropState = imageCropper.cropState 
if(cropState != null) ImageCropperDialog(state = cropState)
```
That's it !


## Customization
To customize the ui of the image cropper you can provide a different implementation of `CropperStyle` to the cropper dialog.
You can also use the `cropperStyle` factory function. example :
```kotlin
ImageCropperDialog(
    state = cropState,
    style = cropperStyle(
        overlay = Color.Red.copy(alpha = .5f),
        autoZoom = false,
        guidelines = null,
    )
)
```

## Use different image sources
Krop makes it possible to use different image sources depending on the platform.

### Common 👥🔵
The `crop` function provides overloads for `ImageBitmap`, but it is also possible to use a custom implementation of `ImageSrc`.

#### Available implementations for `ImageSrc` in common code are:
- `ImageBitmapSrc` - takes `ImageBitmap` as a source.

### Android 📱🟢
For android, `crop` function provides overloads for `File`, `Uri` and `ImageStream`.

#### Available implementations for `ImageSrc` in android are:
- `ImageStreamSrc` - takes `ImageStream` as a source.

#### Available implementations for `ImageStream`:
- `UriImageStream` - takes `Uri` and `Context` as sources.
- `FileImageStream` - takes `File` as a source.

### iOS 🍎🟠
For ios, `crop` function provides overloads for `UIImage`, `NSURL` and `PHAsset`.
You can also use `cropPHAssetLocalIdentifier` and `cropPath` to pass string values.

#### Available implementations for `ImageSrc` in ios are:
- `UIImageSrc` - takes `UIImage` as a source.
- `NSURLImageSrc` - takes `NSURL` or `path: String` as sources.
- `PHAssetImageSrc` - takes `PHAsset` or `localIdentifier: String` as sources.

### Desktop 💻🔵
For desktop, `crop` function provides overloads for `File` and `ImageStream`.

#### Available implementations for `ImageSrc` in desktop are:
- `ImageStreamSrc` - takes `ImageStream` as a source.

#### Available implementations for `ImageStream`:
- `FileImageStream` - takes `File` as a source.

## Use extensions
Krop also makes it possible to use different 3rd party libraries using extension modules.

### FileKit
**Step 1:** Add FileKit extension to your project:
```kotlin
commonMain.dependencies {
    implementation("com.attafitamim.krop:extensions-filekit:$version")
}
```

**Step 2:** Use helper functions to load and save images:
```kotlin
val imagePicker = rememberFilePickerLauncher(type = FileKitType.Image) { image ->
    image?.let {
        scope.launch {
            // Convert the selected image to ImageSrc
            val imageSrc = image.toImageSrc()
            
            // Crop the image
            when (val result = imageCropper.cropSrc(imageSrc)) {
                CropResult.Cancelled -> {}
                is CropError -> { /* Handle error */ }
                is CropResult.Success -> {
                    // Convert the cropped image to bytes
                    val bitmap = result.bitmap
                    val bytes = bitmap.encodeToByteArray()
                    
                    // Save the cropped image
                    val file = FileKit.filesDir / "cropped_image.jpg"
                    file.write(bytes)
                }
            }
        }
    }
}
```