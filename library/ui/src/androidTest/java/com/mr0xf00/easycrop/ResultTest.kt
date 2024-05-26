package com.mr0xf00.easycrop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.IntSize
import androidx.test.platform.app.InstrumentationRegistry
import com.mr0xf00.easycrop.core.crop.CropState
import com.mr0xf00.easycrop.core.crop.createResult
import com.mr0xf00.easycrop.core.crop.flipHorizontal
import com.mr0xf00.easycrop.core.crop.rotLeft
import com.mr0xf00.easycrop.core.images.ImageStream
import com.mr0xf00.easycrop.core.images.ImageStreamSrc
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ResultTest {

    private lateinit var state: CropState
    private val full = imageStream("dog.jpg")

    @Before
    fun createState() = runTest {
        val src = ImageStreamSrc(full)
        checkNotNull(src)
        state = CropState(src)
    }

    @Test
    fun image_is_unchanged_when_using_a_full_region_and_no_transform() = runTest {
        val expected = full.openImage()
        val actual = state.createResult(null)
        assertEqual(expected, actual)
    }

    @Test
    fun correct_result_when_applying_transforms() = runTest {
        state.rotLeft()
        state.flipHorizontal()
        val expected = imageStream("dog_rl_fh.png").openImage()
        val actual = state.createResult(null)
        assertEqual(expected, actual)
    }

    @Test
    fun correct_result_when_resizing_region_and_applying_transforms() = runTest {
        state.rotLeft()
        state.flipHorizontal()
        state.region = Rect(Offset(294f, 86f), Size(182f, 143f))
        val expected = imageStream("dog_rl_fh_294_86_182_143.png").openImage()
        val actual = state.createResult(null)?.apply { save() }
        assertEqual(expected, actual)
    }

    private fun imageStream(name: String): ImageStream {
        return ImageStream { javaClass.classLoader!!.getResourceAsStream(name) }
    }
}

private fun ImageStream.openImage(): ImageBitmap {
    return BitmapFactory.decodeStream(openStream(), null, null)?.asImageBitmap()
        ?: error("Image $this cannot be opened")
}

private fun assertEqual(expected: ImageBitmap, actual: ImageBitmap?) {
    checkNotNull(actual)
    Assert.assertEquals(
        IntSize(expected.width, expected.height),
        IntSize(actual.width, actual.height)
    )
    Assert.assertArrayEquals(
        expected.toPixelMap().buffer,
        actual.toPixelMap().buffer
    )
}

private fun ImageBitmap.save() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    context.filesDir.resolve("result.png").outputStream().use { stream ->
        asAndroidBitmap().compress(
            Bitmap.CompressFormat.PNG, 100, stream
        )
    }
}
