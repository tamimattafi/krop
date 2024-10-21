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
<p align="center">
<img height="500" src="art/preview.gif"/>
</p>

## Getting Started

#### 1. Add Dependencies
Version: 

[![Krop Release](https://img.shields.io/github/release/tamimattafi/krop.svg?style=for-the-badge&color=darkgreen)](https://github.com/tamimattafi/krop/releases)

**Option 1:** Add the `ui` module to use the crop dialog out of the box:
```kotlin
commonMain.dependencies {
    implementation("com.attafitamim.krop:ui:$version")
}
```

**Option 2:** If you are looking for a custom design, use the `core` module instead:
```kotlin
commonMain.dependencies {
    implementation("com.attafitamim.krop:core:$version")
}
```
For hints on how to use `core` logic for a custom design, check sources of the `ui` module.

#### 2. Create an `ImageCropper` instance
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
#### 3. Crop
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
#### 4. Show the crop dialog
```kotlin
val cropState = imageCropper.cropState 
if(cropState != null) ImageCropperDialog(state = cropState)
```
That's it !
### Using different sources
The ```crop``` function provides overloads for `ImageBitmap`, `Uri`, `File`, but it is also possible to use a custom `ImageSrc`.

You can use the ```rememberImagePicker``` function to easily pick an image and crop it :
```kotlin
val scope = rememberCoroutineScope()
val context = LocalContext.current
val imagePicker = rememberImagePicker(onImage = { uri ->
    scope.launch {
        val result = imageCropper.crop(uri, context)
    }
})
```

### Customization 
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
