package com.attafitamim.krop.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.cropState
import com.attafitamim.krop.core.crop.flipX
import com.attafitamim.krop.core.crop.getTransformedImageRect
import com.attafitamim.krop.core.crop.rotLeft
import com.attafitamim.krop.core.images.DecodeParams
import com.attafitamim.krop.core.images.DecodeResult
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.utils.containsInclusive
import com.attafitamim.krop.core.utils.resize
import com.attafitamim.krop.core.utils.roundOut
import com.attafitamim.krop.core.utils.scale
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CropStateTest {
    private val size = IntSize(500, 600)
    private lateinit var state: CropState

    @Before
    fun createState() {
        state = cropState(emptyImage(size))
    }

    @Test
    fun `Region is initialized to image rect`() {
        Assert.assertEquals(size.toIntRect(), state.region.roundOut())
    }

    @Test
    fun `Region is inside image rect after offset`() {
        state.region = state.region.translate(100f, 100f)
        assertRegionInImageRect()
    }

    @Test
    fun `Region is inside image rect after resize`() {
        state.region = state.region
            .scale(.5f, .5f)
            .resize(Offset(1f, 1f), Offset(size.width * 2f, size.height * 2f), 10f)
        assertRegionInImageRect()
    }

    @Test
    fun `Region is inside image rect after resize with aspect lock`() {
        state.aspectLock = true
        state.region = state.region.transformCropRect()
        assertRegionInImageRect()
    }

    @Test
    fun `Region is inside transformed image rect after resize`() {
        state.rotLeft()
        state.flipX()
        state.region = state.region.transformCropRect()
        assertRegionInImageRect()
    }

    private fun Rect.transformCropRect(): Rect {
        return scale(.5f, .5f)
            .resize(Offset(1f, 1f), Offset(size.width * 2f, size.height * 2f), 10f)
    }

    private fun assertRegionInImageRect() {
        val imgRect = getTransformedImageRect(state.transform, state.src.size)
        Assert.assertTrue(imgRect.roundOut().containsInclusive(state.region.roundOut()))
    }
}

fun emptyImage(size: IntSize) = object : ImageSrc {
    override val size: IntSize get() = size
    override suspend fun open(params: DecodeParams): DecodeResult? = null
}